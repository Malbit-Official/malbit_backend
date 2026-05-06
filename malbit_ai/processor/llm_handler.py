import boto3
import json

bedrock_client = boto3.client("bedrock-runtime", region_name="ap-northeast-2")

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