import os
import boto3
import json
import re
from dotenv import load_dotenv

load_dotenv()

bedrock_client = boto3.client(
    "bedrock-runtime", 
    region_name="ap-northeast-2",
    aws_access_key_id=os.getenv("AWS_ACCESS_KEY_ID"),
    aws_secret_access_key=os.getenv("AWS_SECRET_ACCESS_KEY")
)

def refine_text_with_llm(raw_text: str, llm_model: str) -> str:
    if not raw_text.strip():
        return ""

    prompt = f"""You are "Malbbit," a professional AI communication assistant specialized in reconstructing dysarthric speech into clear, natural business Korean.
    ### Instructions
    1. Deduplicate meaningless repetitions.
    2. If a word clearly breaks the context (STT hallucination), replace it with a plausible term or omit it.
    3. Use professional Polite/Honorific Korean.
    4. Return ONLY the final corrected Korean sentence without any explanations.

    ### Input to Process:
    {raw_text}

    ### Final Reconstructed Korean:"""

    native_request = {
        "anthropic_version": "bedrock-2023-05-31",
        "max_tokens": 500,
        "temperature": 0,
        "messages": [{"role": "user", "content": [{"type": "text", "text": prompt}]}],
    }

    try:
        response = bedrock_client.invoke_model(
            modelId=llm_model,
            contentType="application/json",
            accept="application/json",
            body=json.dumps(native_request)
        )
        response_body = json.loads(response["body"].read())
        return response_body["content"][0]["text"].strip()
    except Exception as e:
        print(f"[Bedrock LLM Error] {e}")
        return raw_text
    
# 할일 요약
def summarize_meeting_and_schedule(raw_text: str, current_date: str, llm_model) -> dict:
    if not raw_text.strip():
        return {"summary_text": "분석할 업무 내용이 없습니다.", "checklists": [], "schedules": []}
    
    # 직장 생활 적응 및 업무 관리 특화 프롬프트
    prompt = f"""
    You are the "Malbit Smart Workplace Assistant," specialized in supporting workers with speech or language impairments. 
    Analyze the provided transcript from a workplace setting (Cafe, Retail, Office, etc.) and extract structured tasks.

    ### Core Objectives (Focus on "WHAT TO DO"):
    1. **Contextual Summary**: Provide a 2-3 sentence summary in Korean. Start with the most urgent task.
    2. **Checklist Generation**: Extract small, immediate sub-tasks or items to bring (e.g., "우유 유통기한 확인", "앞치마 챙기기").
    3. **Task Scheduling**: Identify tasks with specific or implied deadlines.

    ### Constraints & Rules:
    - **Language**: Output `summary_text`, `checklists`, and `title` in natural, polite Korean.
    - **DateTime Standard**: 
        - Current Reference Date: {current_date}
        - Convert relative time (e.g., "모레", "퇴근 전", "30분 뒤") to absolute "YYYY-MM-DD" and "HH:MM".
    - **Importance Logic**: 
        - High: Direct orders from supervisors or time-critical tasks (e.g., "지금 바로", "오늘까지").
        - Medium: Routine tasks with a set time.
        - Low: General reminders or future ideas.

    ### JSON Schema:
    {{
      "summary_text": "가장 중요한 업무를 중심으로 한 전체 흐름 요약",
      "checklists": ["즉시 수행하거나 확인해야 할 세부 항목"],
      "schedules": [
        {{
          "title": "행동 중심의 할 일 요약 (예: 재고 발주서 전송)",
          "category": "업무 | 미팅 | 휴식 | 개인",
          "date": "YYYY-MM-DD",
          "time": "HH:MM",
          "importance": "High | Medium | Low"
        }}
      ]
    }}

    ### Transcript to Analyze:
    {raw_text}

    ### Output (JSON ONLY):
    """

    native_request = {
        "anthropic_version": "bedrock-2023-05-31",
        "max_tokens": 1500,
        "temperature": 0,
        "messages": [
            {"role": "user", "content": [{"type": "text", "text": prompt}]}
        ],
    }

    try:
        response = bedrock_client.invoke_model(
            modelId=llm_model,
            contentType="application/json",
            accept="application/json",
            body=json.dumps(native_request)
        )
        
        response_body = json.loads(response["body"].read())
        raw_output = response_body["content"][0]["text"].strip()
        
        # LLM이 JSON 마크다운 형식을 포함할 경우를 대비한 정제 로직
        if "```json" in raw_output:
            raw_output = re.search(r"```json\s*(.*?)\s*```", raw_output, re.DOTALL).group(1)
        
        return json.loads(raw_output)

    except json.JSONDecodeError:
        print(" [Error] LLM 응답이 유효한 JSON 형식이 아닙니다.")
        return {"summary_text": "결과 파싱 오류", "checklists": [], "schedules": []}
    except Exception as e:
        print(f" [Bedrock Meeting Error] {e}")
        return {"summary_text": "요약 중 오류 발생", "checklists": [], "schedules": []}
