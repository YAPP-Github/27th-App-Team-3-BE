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
    INDEX            idx_couple_info_user1_id (user1_id),
    INDEX            idx_couple_info_user2_id (user2_id)
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

create table users
(
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    email             VARCHAR(255)                      not null,
    name              varchar(255)                      not null,
    oauth_provider_id varchar(255)                      not null,
    oauth_provider    enum ('APPLE', 'GOOGLE', 'KAKAO') not null,
    constraint UK7b9eycvssw4cm3b2cakcq67x
        unique (oauth_provider, oauth_provider_id)
);

-- 목표 테이블
CREATE TABLE goals
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    couple_id    BIGINT       NOT NULL,
    name         VARCHAR(255) NOT NULL,
    repeat_cycle VARCHAR(20)  NOT NULL COMMENT 'DAILY, WEEKLY, MONTHLY',
    repeat_count INT          NOT NULL,
    start_date   DATE         NOT NULL,
    has_end_date BOOLEAN      NOT NULL DEFAULT FALSE,
    end_date     DATE NULL,
    deleted_at   TIMESTAMP NULL,
    goal_status  VARCHAR(50)  NOT NULL COMMENT 'NOT_STARTED, IN_PROGRESS, COMPLETED, DELETED',
    goal_icon    VARCHAR(20)  NOT NULL,
    stamp_type   ENUM('CLOVER', 'FLOWER', 'HEART', 'MOON', 'NOTE') NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_goals_couple_deleted_start (couple_id, deleted_at, start_date),
    INDEX idx_goals_status_enddate (goal_status, has_end_date, end_date, deleted_at),
    INDEX idx_goals_status_startdate (goal_status, start_date, deleted_at)
);

-- 포토로그 테이블 (인증샷)
CREATE TABLE photo_log
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    goal_id           BIGINT       NOT NULL,
    user_id           BIGINT       NOT NULL,
    verification_date DATE         NOT NULL,
    uploaded_at       TIMESTAMP    NOT NULL,
    file_name         VARCHAR(255) NOT NULL,
    comment           VARCHAR(30)  NULL,
    reaction          VARCHAR(20)  NULL COMMENT 'EMOJI1, EMOJI2, EMOJI3, EMOJI4, EMOJI5',
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_photo_log_goal_user_date (goal_id, user_id, verification_date),
    INDEX idx_photo_log_goal_date (goal_id, verification_date)
);


-- 사용자 추가 정보 테이블
CREATE TABLE user_addition_info
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL UNIQUE,
    nickname   VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX      idx_user_addition_info_user_id (user_id)
);

-- 소셜 토큰 테이블 (회원탈퇴 시 토큰 revoke용)
CREATE TABLE social_tokens
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id       BIGINT                            NOT NULL,
    provider      ENUM ('APPLE', 'GOOGLE', 'KAKAO') NOT NULL,
    refresh_token VARCHAR(1024)                     NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_social_tokens_user_provider (user_id, provider),
    INDEX idx_social_tokens_user_id (user_id)
);


-- 알림 테이블
CREATE TABLE notifications
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    type       VARCHAR(50)  NOT NULL COMMENT 'PARTNER_CONNECTED, POKE, GOAL_COMPLETED, REACTION, DAILY_GOAL_ACHIEVED, GOAL_ENDED',
    title      VARCHAR(255) NOT NULL,
    body       VARCHAR(500) NOT NULL,
    deep_link  VARCHAR(200) NULL,
    is_read    BOOLEAN      NOT NULL DEFAULT FALSE,
    read_at    TIMESTAMP NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_notifications_user_read (user_id, is_read, created_at)
);

-- FCM 토큰 테이블
CREATE TABLE fcm_tokens
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    token      VARCHAR(500) NOT NULL,
    device_id  VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_fcm_tokens_user (user_id),
    UNIQUE KEY uk_fcm_tokens_user_device (user_id, device_id)
);

-- 알림 설정 테이블
CREATE TABLE notification_settings
(
    id                             BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id                        BIGINT  NOT NULL UNIQUE,
    is_night_notification_enabled  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at                     TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at                     TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_notification_settings_user (user_id)
);

-- 찌르기 테이블
CREATE TABLE pokes
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    sender_id   BIGINT    NOT NULL,
    receiver_id BIGINT    NOT NULL,
    goal_id     BIGINT    NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_pokes_sender_goal (sender_id, goal_id, created_at)
);

-- 스탬프 히스토리 테이블
CREATE TABLE stamp_history
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    photolog_id BIGINT      NOT NULL UNIQUE,
    goal_id     BIGINT      NOT NULL,
    user_id     BIGINT      NOT NULL,
    stamp_date  DATE        NOT NULL,
    stamp_color VARCHAR(20) NOT NULL COMMENT 'GREEN400, BLUE400, YELLOW400, PINK400, PINK300, PINK200, ORANGE400, PURPLE400',
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_stamp_history_photolog (photolog_id),
    INDEX idx_stamp_history_goal_user_date (goal_id, user_id, stamp_date)
);
