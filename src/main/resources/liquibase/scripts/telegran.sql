-- liquibase formatted sql

-- changeset dbogomolov:2023-12-12
CREATE TABLE notification_task(
    id bigserial PRIMARY KEY,
    chat_id bigint,
    text VARCHAR,
    message_date TIMESTAMP
)
