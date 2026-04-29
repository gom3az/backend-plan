CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

INSERT INTO user_roles (user_id, role)
SELECT id, regexp_replace(regexp_replace(roles, '[\[\]{}]', '', 'g'), '"', '', 'g')
FROM users WHERE roles IS NOT NULL;

ALTER TABLE users DROP COLUMN roles;