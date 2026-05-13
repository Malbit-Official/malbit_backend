# 상황별 발화 추천 서비스
import json
import re
from core.llm_client import bedrock_client
from core.config import Config

def suggest_speech_by_situation(category: str, user_input: str = None) -> dict:
    """
    사용자가 선택한 카테고리나 직접 입력한 상황에 맞춰
    정중하고 세련된 한국어 문장 3개를 추천합니다.
    """

    # AI에게 전달할 상황 문맥 조립
    situation_context = user_input if user_input and user_input.strip() else f"{category} 관련 대화"

    # '말빛' 전용 페르소나 주입 프롬프트
    prompt = f"""
    당신은 언어 교정 전문가이자 비즈니스 커뮤니케이션 코치인 '말빛'입니다.
    구음장애가 있는 사용자가 직장이나 일상생활에서 신뢰감을 줄 수 있도록 상황에 맞는 세련된 문장을 추천해야 합니다.

    ### 상황 (Context):
    - 카테고리: {category}
    - 세부 상황: {situation_context}

    ### 지시 사항 (Instructions):
    1. 위 상황에서 바로 사용할 수 있는 정중하고 명확한 한국어 문장 5개를 생성하세요.
    2. 각 문장마다 어떤 상황에 쓰면 좋을지 20자 이내의 짧은 '사용 팁(tip)'을 덧붙이세요.
    3. 결과는 반드시 아래의 JSON 형식으로만 출력하세요. 다른 설명은 생략합니다.

    ### 응답 형식 (JSON Schema):
        {{
          "recommendations": [
            {{ "speech": "문장1", "tip": "팁1" }},
            {{ "speech": "문장2", "tip": "팁2" }},
            {{ "speech": "문장3", "tip": "팁3" }},
            {{ "speech": "문장4", "tip": "팁4" }},
            {{ "speech": "문장5", "tip": "팁5" }}
          ]
        }}
    """

    native_request = {
        "anthropic_version": "bedrock-2023-05-31",
        "max_tokens": 1500,
        "temperature": 0.3, # 조금의 창의성을 위해 0.3 설정
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

        # JSON 정제 (마크다운 기호 제거)
        if "```json" in raw_output:
            raw_output = re.search(r"```json\s*(.*?)\s*```", raw_output, re.DOTALL).group(1)

        return json.loads(raw_output)

    except Exception as e:
        print(f" [Speech Service Error] {e}")
        # 에러 발생 시 빈 리스트 반환
        return {"recommendations": []}