# LLM 기능 모음
import json
from core.llm_client import bedrock_client
from core.config import Config

def refine_text_with_llm(raw_text: str) -> str:
    """구음장애 텍스트를 비즈니스용 한국어로 보정"""
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

    ### Final Reconstructed Korean:
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