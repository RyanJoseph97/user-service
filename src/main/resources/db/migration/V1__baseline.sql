-- Baseline schema for user-service (PostgreSQL).
-- Generated from the JPA entity model via Hibernate DDL export, then hand-formatted
-- and given readable constraint names. This is the V1 Flyway baseline; all future
-- schema changes go in new V2+ migrations. Hibernate runs in `validate` mode against
-- this schema in the docker/prod profile, so column names and types must stay in sync
-- with the entities.

create table users (
    id                   bigserial      not null,
    username             varchar(255)   not null,
    email                varchar(255)   not null,
    password             varchar(255)   not null,
    name                 varchar(255),
    bio                  varchar(1000),
    location             varchar(255),
    latitude             float8,
    longitude            float8,
    profile_picture_url  varchar(1000),
    private_profile      boolean        not null,
    account_status       varchar(255)   not null,
    date_joined          date           not null,
    primary key (id),
    constraint uk_users_username unique (username),
    constraint uk_users_email    unique (email)
);

create table user_follows (
    id           bigserial   not null,
    follower_id  int8        not null,
    followee_id  int8        not null,
    followed_at  timestamp   not null,
    primary key (id),
    constraint uk_user_follows_follower_followee unique (follower_id, followee_id),
    constraint fk_user_follows_follower foreign key (follower_id) references users,
    constraint fk_user_follows_followee foreign key (followee_id) references users
);
create index idx_follow_followee_id on user_follows (followee_id);

create table follow_requests (
    id                  bigserial      not null,
    requester_username  varchar(255)   not null,
    target_username     varchar(255)   not null,
    status              varchar(255)   not null,
    created_at          timestamp      not null,
    primary key (id),
    constraint uk_follow_requests_requester_target unique (requester_username, target_username)
);
create index idx_follow_request_target_username on follow_requests (target_username);

create table notifications (
    id                  bigserial      not null,
    recipient_username  varchar(255)   not null,
    actor_username      varchar(255),
    type                varchar(255)   not null,
    entity_id           varchar(255),
    message             TEXT           not null,
    seen                boolean        not null,
    created_at          timestamp      not null,
    primary key (id)
);
create index idx_notification_recipient_username on notifications (recipient_username);
create index idx_notification_recipient_seen     on notifications (recipient_username, seen);

create table messages (
    id                  bigserial      not null,
    sender_username     varchar(255)   not null,
    recipient_username  varchar(255)   not null,
    content             TEXT           not null,
    shared_event_id     varchar(255),
    sent_at             timestamp      not null,
    read_at             timestamp,
    primary key (id)
);
create index idx_message_sender_recipient    on messages (sender_username, recipient_username);
create index idx_message_recipient_username  on messages (recipient_username);

create table refresh_tokens (
    id         bigserial      not null,
    token      varchar(512)   not null,
    username   varchar(255)   not null,
    issued_at  timestamp      not null,
    expires_at timestamp      not null,
    revoked    boolean        not null,
    primary key (id),
    constraint uk_refresh_tokens_token unique (token)
);
create index idx_refresh_token_username on refresh_tokens (username);
