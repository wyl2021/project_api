-- 先为null值的email设置默认值
UPDATE user SET email = 'default@example.com' WHERE email IS NULL;
-- 然后修改password和email字段约束
ALTER TABLE user MODIFY password VARCHAR(255) NOT NULL;
ALTER TABLE user MODIFY email VARCHAR(100) NOT NULL;