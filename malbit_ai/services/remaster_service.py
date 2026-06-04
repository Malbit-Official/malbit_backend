# 단문 리마스터링 서비스
from core.stt_engine import transcribe_audio
from services.llm_service import refine_text_with_llm
from core.config import Config

import jiwer
from nltk.translate.bleu_score import sentence_bleu, SmoothingFunction

def run_remastering(asr_pipe, audio_path: str, ground_truth: str = None) -> dict:
    """
    음성 -> STT -> LLM 보정 프로세스 실행 및 AI 성능 지표 측정
    """

    # STT로 날것의 텍스트 추출
    raw_text = transcribe_audio(asr_pipe, audio_path)

    # LLM으로 정중한 문장으로 보정 
    refined_text = refine_text_with_llm(raw_text)

    # 기본 반환 데이터 포맷 설정
    result = {
        "raw": raw_text,
        "refined": refined_text,
        "metrics_enabled": False
    }

    ground_truth = "오늘 점심 약을 까먹지 말고 꼭 먹어야 합니다"
    # ground_truth = "선생님 이따가 이메일로 과제 보낼 테니까 확인해 주세요"
    #ground_truth = "아 진짜 짜증 나고 답답하니까 그냥 다 때려치우고 싶어"

    # AI 성능 지표 계산
    if ground_truth and ground_truth.strip():
        try:
            try:
                calculated_cer = jiwer.process_characters(ground_truth, raw_text).cer * 100
            except AttributeError:
                calculated_cer = jiwer.cer(ground_truth, raw_text) * 100

            # 문장 유사도(BLEU) 계산
            reference = [ground_truth.split()]
            candidate = refined_text.split()
            chencherry = SmoothingFunction()
            calculated_bleu = sentence_bleu(reference, candidate, smoothing_function=chencherry.method1) * 100

            # 결과에 수치 데이터 추가
            result["metrics_enabled"] = True
            result["cer_percentage"] = round(calculated_cer, 1)
            result["bleu_percentage"] = round(calculated_bleu, 1)

            # 실시간 결과 출력
            print(f"\n================ [AI PERFORMANCE LOG] ================")
            print(f" [정답 문장(Target)] : {ground_truth}")
            print(f" [인식 문장(Raw STT)] : {raw_text}   ➡️   [CER: {calculated_cer:.1f}%]")
            print(f" [교정 문장(Refined)] : {refined_text}   ➡️   [BLEU: {calculated_bleu:.1f}%]")
            print(f"======================================================\n")

        except Exception as metric_error:
            print(f"\n [Metrics Calculation Error] 내부 연산 실패: {metric_error}")
            print(f" [Fallback] 수치 없이 텍스트 로그만 우선 출력합니다.")
            print(f"================ [AI LOG FALLBACK] ================")
            print(f" [정답] : {ground_truth}")
            print(f" [인식] : {raw_text}")
            print(f" [교정] : {refined_text}")
            print(f"===================================================\n")

    return result