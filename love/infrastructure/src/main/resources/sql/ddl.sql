-- 초대 코드 테이블
CREATE TABLE invite_codes
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    code       VARCHAR(10) NOT NULL UNIQUE,
    creator_id BIGINT      NOT NULL,
    used_at    TIMESTAMP NULL,
    used_by_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX      idx_invite_codes_creator_id (creator_id)
);

-- 커플 테이블
CREATE TABLE couple_info
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user1_id         BIGINT NOT NULL,
    user2_id         BIGINT NOT NULL,
    invite_code_id   BIGINT NOT NULL UNIQUE,
    anniversary_date DATE NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX            idx_couples_info_user1_id (user1_id),
    INDEX            idx_couples_info_user2_id (user2_id)
);

-- 온보딩 온보딩테이블
CREATE TABLE user_onboarding_info
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id      BIGINT      NOT NULL UNIQUE,
    status       VARCHAR(30) NOT NULL,
    completed_at TIMESTAMP NULL,
    created_at   TIMESTAMP   NOT NULL,
    updated_at   TIMESTAMP   NOT NULL,
    INDEX        idx_user_onboarding_info_user_id (user_id)
);
