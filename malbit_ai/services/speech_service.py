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
        <role>
        You are "Malbit," a professional language coach and business communication expert specializing in helping users with dysarthria communicate with confidence and credibility in workplace and daily life situations.
        </role>

        <instructions>
        Given the context below, generate exactly 5 refined, immediately usable Korean sentences appropriate for the situation.

        For each sentence, provide a short usage tip (20 characters or fewer in Korean) that describes when or how to best use it.

        **Sentence Quality Guidelines:**
        - Use polite, professional Korean (해요체 or 합쇼체 as appropriate)
        - Keep sentences concise and natural — avoid overly formal or stiff phrasing
        - Each sentence should feel distinct, covering different nuances of the same situation
        - Prioritize sentences that build trust and credibility for the speaker
        </instructions>

        <context>
        - Category: {category}
        - Situation: {situation_context}
        </context>

        <constraints>
        Output ONLY the JSON below. Do NOT include explanations, markdown code fences, or any extra text.

        {{
        "recommendations": [
            {{"speech": "추천 문장 1", "tip": "20자 이내 사용 팁"}},
            {{"speech": "추천 문장 2", "tip": "20자 이내 사용 팁"}},
            {{"speech": "추천 문장 3", "tip": "20자 이내 사용 팁"}},
            {{"speech": "추천 문장 4", "tip": "20자 이내 사용 팁"}},
            {{"speech": "추천 문장 5", "tip": "20자 이내 사용 팁"}}
        ]
        }}
        </constraints>
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