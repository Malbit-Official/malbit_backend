# Calendar API 연동 및 일정 등록 로직
import httpx
import os
from dotenv import load_dotenv

load_dotenv()

# 서버 주소 
BACKEND_URL = os.getenv("BACKEND_URL", "http://localhost:8080")
CREATE_TASK_URL = f"{BACKEND_URL}/api/calendar/manual"

async def sync_schedules_to_backend(schedules: list, auth_header: str):
    
    async with httpx.AsyncClient() as client:
        for item in schedules:
            
            payload = {
                "content": item["title"], 
                "category": item["category"],
                "start_at": f"{item['date']} {item['time']}:00", 
                "end_at": f"{item['date']} 23:59:59"        
            }
            
            # 클라이언트의 토클을 그대로 백엔드에 전달하여 사용자 인증
            headers = {
                "Authorization": auth_header,
                "Content-Type": "application/json"
            }
            
            try:
                response = await client.post(
                    f"{BACKEND_URL}/api/calendar/manual",
                    json=payload, 
                    headers=headers
                )
                
                if response.status_code == 200:
                    print(f" [Sync] 저장 성공: {item['title']}")
                else:
                    print(f" [Sync Fail] {response.status_code}: {response.text}")
            except Exception as e:
                print(f" [Sync Error] {e}")