-- =========================
-- 1. 관리자 계정 (최우선)
-- =========================
-- 관리자 계정 (username: admin, password: admin1234)
INSERT INTO users (username, email, password, phone, address, role, created_at)
VALUES ('admin', 'admin@arduino.com', '$2a$10$t3l/xedx6VNUbQeoPHrBpu5lCXs.JQDp2VjT7tzstfbFwXJm6RM/O', '010-0000-0000', '서울시 강남구', 'ADMIN', CURRENT_TIMESTAMP);

-- 테스트 사용자 (username: test, password: test1234)
INSERT INTO users (username, email, password, phone, address, role, created_at)
VALUES ('test', 'test@test.com', '$2a$10$eXTDbOj/gPQSamwi96mytuZ3vP7o6/uJSkgLU6p13QenlTGHQI/j6', '010-1111-1111', '서울시 서초구', 'USER', CURRENT_TIMESTAMP);

-- 테스트 사용자 (username: stc8652, password: 12345678)
INSERT INTO users (username, email, password, phone, address, role, created_at)
VALUES ('stc8652', 'ekdms8652@gmail.com', '$2a$10$sBPB2P/zaLDW4VL8G4g7NOph5psbwC42offhWmwNje3vjmawWOyO2', '010-7293-9042', '경상남도 밀양시', 'USER', CURRENT_TIMESTAMP);

-- =========================
-- 2. 카테고리 데이터
-- =========================
INSERT INTO categories (name, description, parent_id) VALUES ('아두이노 보드', '다양한 아두이노 보드 제품', NULL);
INSERT INTO categories (name, description, parent_id) VALUES ('센서', '각종 센서 모듈', NULL);
INSERT INTO categories (name, description, parent_id) VALUES ('모터/액추에이터', '모터 및 제어 장치', NULL);
INSERT INTO categories (name, description, parent_id) VALUES ('디스플레이', 'LCD, OLED 등 디스플레이', NULL);
INSERT INTO categories (name, description, parent_id) VALUES ('부품/케이블', '전자부품 및 케이블', NULL);

-- 하위 카테고리
INSERT INTO categories (name, description, parent_id) VALUES ('Arduino UNO', 'Arduino UNO 시리즈', 1);
INSERT INTO categories (name, description, parent_id) VALUES ('Arduino MEGA', 'Arduino MEGA 시리즈', 1);
INSERT INTO categories (name, description, parent_id) VALUES ('온도/습도 센서', '온습도 측정 센서', 2);
INSERT INTO categories (name, description, parent_id) VALUES ('거리 센서', '거리 측정 센서', 2);
INSERT INTO categories (name, description, parent_id) VALUES ('서보 모터', '서보 모터', 3);

-- =========================
-- 3. 상품 데이터 (15개)
-- =========================
-- Arduino UNO R3
INSERT INTO products (name, description, price, stock_quantity, category_id, manufacturer, model_number, image_url, is_available, created_at)
VALUES ('Arduino UNO R3', '가장 기본적인 아두이노 보드로 초보자에게 적합합니다. ATmega328P 마이크로컨트롤러 탑재',
        28000, 50, 6, 'Arduino', 'A000066', '/uploads/products/arduinouno.png', true, CURRENT_TIMESTAMP);

-- Arduino MEGA 2560
INSERT INTO products (name, description, price, stock_quantity, category_id, manufacturer, model_number, image_url, is_available, created_at)
VALUES ('Arduino MEGA 2560', '더 많은 I/O 핀과 메모리를 제공하는 고급 아두이노 보드. 복잡한 프로젝트에 최적',
        45000, 30, 7, 'Arduino', 'A000067', '/uploads/products/arduinomega.jpg', true, CURRENT_TIMESTAMP);

-- Arduino Nano
INSERT INTO products (name, description, price, stock_quantity, category_id, manufacturer, model_number, image_url, is_available, created_at)
VALUES ('Arduino Nano', '소형 사이즈의 아두이노 보드. 브레드보드에 바로 장착 가능',
        18000, 80, 6, 'Arduino', 'A000005', '/uploads/products/arduinonano.png', true, CURRENT_TIMESTAMP);

-- DHT11 (이미지 없음 → placeholder 유지 또는 새 파일 알려줘!)
INSERT INTO products (name, description, price, stock_quantity, category_id, manufacturer, model_number, image_url, is_available, created_at)
VALUES ('DHT11 온습도 센서', '온도와 습도를 동시에 측정하는 디지털 센서. 저렴하고 사용이 간편함',
        5000, 100, 8, 'DFRobot', 'DHT11', '/uploads/products/pir.jpg', true, CURRENT_TIMESTAMP);

-- DHT22 (이미지 없음)
INSERT INTO products (name, description, price, stock_quantity, category_id, manufacturer, model_number, image_url, is_available, created_at)
VALUES ('DHT22 온습도 센서', 'DHT11보다 정확한 고급 온습도 센서. 더 넓은 측정 범위 제공',
        12000, 60, 8, 'DFRobot', 'DHT22', '/uploads/products/pir.jpg', true, CURRENT_TIMESTAMP);

-- HC-SR04
INSERT INTO products (name, description, price, stock_quantity, category_id, manufacturer, model_number, image_url, is_available, created_at)
VALUES ('HC-SR04 초음파 센서', '초음파를 이용한 거리 측정 센서. 2cm~400cm 범위 측정 가능',
        3500, 120, 9, 'HCSR', 'HC-SR04', '/uploads/products/pir.jpg', true, CURRENT_TIMESTAMP);

-- PIR 센서
INSERT INTO products (name, description, price, stock_quantity, category_id, manufacturer, model_number, image_url, is_available, created_at)
VALUES ('PIR 모션 센서', '사람의 움직임을 감지하는 적외선 센서. 보안 시스템에 활용',
        4500, 70, 2, 'HC', 'HC-SR501', '/uploads/products/pir.jpg', true, CURRENT_TIMESTAMP);

-- SG90 서보 모터
INSERT INTO products (name, description, price, stock_quantity, category_id, manufacturer, model_number, image_url, is_available, created_at)
VALUES ('SG90 서보 모터', '소형 서보 모터. 180도 회전 가능, 로봇 팔이나 카메라 팬틸트에 사용',
        6000, 90, 10, 'TowerPro', 'SG90', '/uploads/products/moter.jpg', true, CURRENT_TIMESTAMP);

-- MG996R
INSERT INTO products (name, description, price, stock_quantity, category_id, manufacturer, model_number, image_url, is_available, created_at)
VALUES ('MG996R 서보 모터', '고토크 메탈 기어 서보 모터. 무거운 물체 제어 가능',
        15000, 40, 10, 'TowerPro', 'MG996R', '/uploads/products/moter.jpg', true, CURRENT_TIMESTAMP);

-- 28BYJ-48 스텝 모터
INSERT INTO products (name, description, price, stock_quantity, category_id, manufacturer, model_number, image_url, is_available, created_at)
VALUES ('28BYJ-48 스텝 모터', '5V 스텝 모터 + ULN2003 드라이버 보드 세트. 정밀한 위치 제어',
        8000, 55, 3, 'Generic', '28BYJ-48', '/uploads/products/submotor.png', true, CURRENT_TIMESTAMP);

-- LCD 디스플레이
INSERT INTO products (name, description, price, stock_quantity, category_id, manufacturer, model_number, image_url, is_available, created_at)
VALUES ('16x2 LCD 디스플레이', 'I2C 인터페이스 16x2 문자 LCD. 쉬운 연결과 적은 핀 사용',
        9000, 65, 4, 'Generic', 'LCD1602-I2C', '/uploads/products/lcddisplay.jpg', true, CURRENT_TIMESTAMP);

-- OLED 디스플레이
INSERT INTO products (name, description, price, stock_quantity, category_id, manufacturer, model_number, image_url, is_available, created_at)
VALUES ('0.96인치 OLED 디스플레이', 'I2C 128x64 해상도 OLED. 선명한 화면과 저전력 소비',
        12000, 50, 4, 'Generic', 'SSD1306', '/uploads/products/leddisplay.jpg', true, CURRENT_TIMESTAMP);

-- 점퍼 와이어
INSERT INTO products (name, description, price, stock_quantity, category_id, manufacturer, model_number, image_url, is_available, created_at)
VALUES ('점퍼 와이어 세트 120pcs', '다양한 길이의 Male-Male, Male-Female, Female-Female 점퍼 와이어',
        8000, 100, 5, 'Generic', 'JW-120', '/uploads/products/jumpline.jpg', true, CURRENT_TIMESTAMP);

-- 브레드보드
INSERT INTO products (name, description, price, stock_quantity, category_id, manufacturer, model_number, image_url, is_available, created_at)
VALUES ('브레드보드 830홀', '납땜 없이 회로 구성이 가능한 브레드보드. 830 타이포인트',
        5000, 85, 5, 'Generic', 'BB-830', '/uploads/products/breadboard.jpg', true, CURRENT_TIMESTAMP);

-- USB 케이블
INSERT INTO products (name, description, price, stock_quantity, category_id, manufacturer, model_number, image_url, is_available, created_at)
VALUES ('USB 케이블 A-B 타입', '아두이노 UNO/MEGA 연결용 USB 케이블. 1m 길이',
        3000, 120, 5, 'Generic', 'USB-AB-1M', '/uploads/products/usb.jpg', true, CURRENT_TIMESTAMP);