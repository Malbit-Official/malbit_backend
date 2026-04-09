import os
from dataclasses import dataclass
from typing import Any, Dict, List, Union

import evaluate
import librosa
import numpy as np
import soundfile as sf
import torch
from datasets import load_dataset
from transformers import (
    WhisperForConditionalGeneration,
    WhisperProcessor,
    Seq2SeqTrainer,
    Seq2SeqTrainingArguments,
)

MODEL_NAME = "openai/whisper-small"
LANGUAGE = "ko"
TASK = "transcribe"

TRAIN_FILE = "prepared_dataset/meta/train.jsonl"
VALID_FILE = "prepared_dataset/meta/valid.jsonl"
OUTPUT_DIR = "./models/whisper-dysarthria-ko"

TARGET_SR = 16000
MAX_LABEL_LENGTH = 256


def load_audio(audio_path: str, target_sr: int = 16000) -> np.ndarray:
    audio, sr = sf.read(audio_path)

    # stereo면 mono로
    if len(audio.shape) > 1:
        audio = np.mean(audio, axis=1)

    # float32 변환
    audio = audio.astype(np.float32)

    # resample
    if sr != target_sr:
        audio = librosa.resample(audio, orig_sr=sr, target_sr=target_sr)

    return audio


def prepare_dataset(batch, processor):
    audio_path = batch["audio"]
    audio_array = load_audio(audio_path, TARGET_SR)

    batch["input_features"] = processor.feature_extractor(
        audio_array,
        sampling_rate=TARGET_SR,
    ).input_features[0]

    batch["labels"] = processor.tokenizer(
        batch["text"],
        truncation=True,
        max_length=MAX_LABEL_LENGTH,
    ).input_ids

    return batch


@dataclass
class DataCollatorSpeechSeq2SeqWithPadding:
    processor: Any

    def __call__(self, features: List[Dict[str, Union[List[int], torch.Tensor]]]) -> Dict[str, torch.Tensor]:
        input_features = [{"input_features": f["input_features"]} for f in features]
        batch = self.processor.feature_extractor.pad(input_features, return_tensors="pt")

        label_features = [{"input_ids": f["labels"]} for f in features]
        labels_batch = self.processor.tokenizer.pad(label_features, return_tensors="pt")

        labels = labels_batch["input_ids"].masked_fill(labels_batch.attention_mask.ne(1), -100)

        if labels.shape[1] > 0 and (labels[:, 0] == self.processor.tokenizer.bos_token_id).all().cpu().item():
            labels = labels[:, 1:]

        batch["labels"] = labels
        return batch


def main():
    dataset = load_dataset(
        "json",
        data_files={
            "train": TRAIN_FILE,
            "validation": VALID_FILE,
        },
    )

    processor = WhisperProcessor.from_pretrained(
        MODEL_NAME,
        language=LANGUAGE,
        task=TASK,
    )

    model = WhisperForConditionalGeneration.from_pretrained(MODEL_NAME)
    model.generation_config.language = LANGUAGE
    model.generation_config.task = TASK
    model.generation_config.forced_decoder_ids = None
    model.generation_config.suppress_tokens = []

    keep_columns = ["audio", "text"]
    remove_cols_train = [c for c in dataset["train"].column_names if c not in keep_columns]
    remove_cols_valid = [c for c in dataset["validation"].column_names if c not in keep_columns]

    dataset["train"] = dataset["train"].map(
        lambda batch: prepare_dataset(batch, processor),
        remove_columns=remove_cols_train,
    )

    dataset["validation"] = dataset["validation"].map(
        lambda batch: prepare_dataset(batch, processor),
        remove_columns=remove_cols_valid,
    )

    data_collator = DataCollatorSpeechSeq2SeqWithPadding(processor=processor)

    wer_metric = evaluate.load("wer")
    cer_metric = evaluate.load("cer")

    def compute_metrics(pred):
        pred_ids = pred.predictions
        label_ids = pred.label_ids.copy()

        label_ids[label_ids == -100] = processor.tokenizer.pad_token_id

        pred_str = processor.tokenizer.batch_decode(pred_ids, skip_special_tokens=True)
        label_str = processor.tokenizer.batch_decode(label_ids, skip_special_tokens=True)

        wer = 100 * wer_metric.compute(predictions=pred_str, references=label_str)
        cer = 100 * cer_metric.compute(predictions=pred_str, references=label_str)

        return {"wer": wer, "cer": cer}

    training_args = Seq2SeqTrainingArguments(
        output_dir=OUTPUT_DIR,
        per_device_train_batch_size=2,
        per_device_eval_batch_size=2,
        gradient_accumulation_steps=8,
        learning_rate=1e-5,
        warmup_steps=100,
        num_train_epochs=3,
        eval_strategy="steps",
        eval_steps=250,
        save_steps=250,
        logging_steps=25,
        predict_with_generate=True,
        generation_max_length=128,
        save_total_limit=2,
        load_best_model_at_end=True,
        metric_for_best_model="wer",
        greater_is_better=False,
        fp16=torch.cuda.is_available(),
        gradient_checkpointing=True,
        report_to="none",
    )

    trainer = Seq2SeqTrainer(
        model=model,
        args=training_args,
        train_dataset=dataset["train"],
        eval_dataset=dataset["validation"],
        data_collator=data_collator,
        compute_metrics=compute_metrics,
        processing_class=processor.feature_extractor,
    )

    os.makedirs(OUTPUT_DIR, exist_ok=True)
    trainer.train()
    trainer.save_model(OUTPUT_DIR)
    processor.save_pretrained(OUTPUT_DIR)
    print(f"saved model to: {OUTPUT_DIR}")


if __name__ == "__main__":
    main()