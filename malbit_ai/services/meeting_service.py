# 회의록 요약 및 할 일 추출 서비스
import json
import re
from core.stt_engine import transcribe_audio
from core.llm_client import bedrock_client
from core.config import Config

def summarize_meeting_and_schedule(audio_path: str, current_date: str, asr_pipe) -> dict:
    """
    회의 음성을 텍스트로 변환하고, LLM을 통해 요약 및 일정을 추출합니다.
    """
    # STT 수행 (음성 -> 텍스트)
    raw_text = transcribe_audio(asr_pipe, audio_path)

    if not raw_text.strip():
        return {
            "raw_text": "",
            "summary_text": "분석할 업무 내용이 없습니다.",
            "checklists": [],
            "schedules": []
        }

    # LLM 프롬프트 설정 (말빛 스마트 워크플레이스 어시스턴트 페르소나)
    prompt = f"""
    You are the "Malbit Smart Workplace Assistant," specialized in supporting workers with speech or language impairments.
    Analyze the provided transcript from a workplace setting and extract structured tasks.

    ### Core Objectives:
    1. **Contextual Summary**: Provide a 2-3 sentence summary in Korean. Start with the most urgent task.
    2. **Checklist Generation**: Extract small, immediate sub-tasks (e.g., "우유 유통기한 확인").
    3. **Task Scheduling**: Identify tasks with specific or implied deadlines.

    ### Constraints:
    - Language: Korean (Polite/Natural)
    - Current Reference Date: {current_date}
    - Convert relative time (e.g., "내일", "3시") to absolute "YYYY-MM-DD" and "HH:MM".

    ### JSON Schema:
    {{
      "summary_text": "요약 내용",
      "checklists": ["할 일 1", "할 일 2"],
      "schedules": [
        {{
          "title": "일정 제목",
          "category": "업무 | 미팅 | 휴식 | 개인",
          "date": "YYYY-MM-DD",
          "time": "HH:MM",
          "importance": "High | Medium | Low"
        }}
      ]
    }}

    ### Transcript:
    {raw_text}

    ### Output (JSON ONLY):
    """

    native_request = {
        "anthropic_version": "bedrock-2023-05-31",
        "max_tokens": 1500,
        "temperature": 0,
        "messages": [{"role": "user", "content": [{"type": "text", "text": prompt}]}],
    }

    try:
        # Bedrock 호출
        response = bedrock_client.invoke_model(
            modelId=Config.LLM_MODEL,
            contentType="application/json",
            accept="application/json",
            body=json.dumps(native_request)
        )

        response_body = json.loads(response["body"].read())
        raw_output = response_body["content"][0]["text"].strip()

        # JSON 정제 로직
        if "```json" in raw_output:
            raw_output = re.search(r"```json\s*(.*?)\s*```", raw_output, re.DOTALL).group(1)

        analysis_result = json.loads(raw_output)

        # 원본 텍스트를 결과에 포함시켜 반환
        return {
            "raw_text": raw_text,
            "summary": analysis_result.get("summary_text", ""),
            "checklists": analysis_result.get("checklists", []),
            "schedules": analysis_result.get("schedules", [])
        }

    except Exception as e:
        print(f" [Meeting Service Error] {e}")
        return {
            "raw_text": raw_text,
            "summary": "요약 중 오류가 발생했습니다.",
            "checklists": [],
            "schedules": []
        }