# 백엔드 일정 동기화 로직
import httpx
from core.config import Config

async def sync_schedules_to_backend(schedules: list, auth_header: str):
    """
    추출된 일정 리스트를 Java 백엔드 서버의 캘린더 API로 전송하여 저장합니다.
    """
    async with httpx.AsyncClient() as client:
        for item in schedules:

            # 백엔드 DTO 규격에 맞게 페어로드 구성
            payload = {
                "content": item["title"],
                "category": item["category"],
                "start_at": f"{item['date']} {item['time']}:00",
                "end_at": f"{item['date']} 23:59:59"
            }

            # 사용자 인증
            headers = {
                "Authorization": auth_header,
                "Content-Type": "application/json"
            }

            try:

                url = f"{Config.BACKEND_URL}/api/calendar/manual"

                response = await client.post(
                    url,
                    json=payload,
                    headers=headers
                )

                if response.status_code in [200, 201]:
                    print(f" [Calendar Service] 저장 성공: {item['title']}")
                else:
                    print(f" [Calendar Service Fail] {response.status_code}: {response.text}")

            except Exception as e:
                print(f" [Calendar Service Error] {e}")