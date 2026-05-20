# LLM 기능 모음
import json
from core.llm_client import bedrock_client
from core.config import Config

def refine_text_with_llm(raw_text: str) -> str:
    """구음장애 텍스트를 비즈니스용 한국어로 보정"""
    if not raw_text.strip():
        return ""

    prompt = f"""
        <role>
        You are "Malbit," a professional AI communication assistant specialized in reconstructing dysarthric or STT-error speech into clear, natural business Korean.
        </role>

        <instructions>
        Reconstruct the input text by following these rules in order:

        1. **Deduplication**: Remove meaningless word or syllable repetitions caused by dysarthria (e.g., "저 저 저는" → "저는").
        2. **Hallucination Correction**: If a word clearly breaks the context (likely an STT error), replace it with the most contextually plausible term or omit it entirely.
        3. **Tone**: Rewrite in professional, polite Korean (합쇼체 or 해요체 as appropriate to context).
        4. **Preserve Intent**: Never add new information or change the original meaning. Only clarify and clean.
        </instructions>

        <constraints>
        - Output ONLY the final reconstructed Korean sentence.
        - Do NOT include explanations, labels, bullet points, or any extra text.
        - If the input is too fragmented to reconstruct confidently, return the best possible interpretation without noting uncertainty.
        </constraints>

        <input>
        {raw_text}
        </input>
    """

    native_request = {
        "anthropic_version": "bedrock-2023-05-31",
        "max_tokens": 500,
        "temperature": 0,
        "messages": [{"role": "user", "content": [{"type": "text", "text": prompt}]}],
    }

    try:
        response = bedrock_client.invoke_model(
            modelId=Config.LLM_MODEL,
            contentType="application/json",
            accept="application/json",
            body=json.dumps(native_request)
        )
        response_body = json.loads(response["body"].read())
        return response_body["content"][0]["text"].strip()

    except Exception as e:
        print(f"[LLM Service Error] {e}")
        return raw_text