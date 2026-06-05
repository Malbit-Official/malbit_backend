-- 1. 카테고리 기본 정보 삽입
INSERT INTO training_category (title, image_url) VALUES 
('주문 받기 연습 ☕', '/images/cafe.png'),
('상품 안내하기 👜', '/images/store.png'),
('업무 보고 연습 📄', '/images/office.png'),
('전화 받기 연습 📞', '/images/phone.png');

-- 2. 해시태그 정보 삽입 (@ElementCollection으로 생성된 테이블)
-- 카테고리 ID(1, 2, 3, 4)와 이미지의 태그를 매칭합니다.
INSERT INTO category_tags (category_id, tag_name) VALUES 
(1, '#주문'), (1, '#확인'),
(2, '#재고'), (2, '#가격'), (2, '#안내'),
(3, '#보고'), (3, '#진행상황'),
(4, '#응대'), (4, '#첫인사'), (4, '#문의');

-- 3. 초기 시나리오 스텝 삽입 (테스트용)
-- 주문 받기 카테고리의 1단계 시나리오
INSERT INTO scenario_step (category_id, step_order, current_situation, guest_script, hint_text, mission_text, retry_script, success_message)
VALUES
(1, 1, '손님이 매장에 들어와 주문을 하려는 상황입니다.',
 '아이스 아메리카노 한 잔 주세요.',
 '네, 아이스 아메리카노 한 잔 맞으실까요?',
 '메뉴를 확인하고 주문을 받으세요.',
 '잘 못 들었어요. 다시 말씀해 주시겠어요?',
 '주문 확인에 성공했습니다!');