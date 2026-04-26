-- 电动滑板车项目数据库管理脚本
-- 用于查看和删除用户数据

-- 1. 查看所有用户信息
SELECT 
    id,
    username,
    email,
    phone,
    full_name,
    role,
    is_student,
    is_senior,
    created_at
FROM users 
ORDER BY created_at DESC;

-- 2. 查看用户数量统计
SELECT 
    COUNT(*) as total_users,
    COUNT(CASE WHEN role = 'ADMIN' THEN 1 END) as admin_users,
    COUNT(CASE WHEN role = 'USER' THEN 1 END) as normal_users,
    COUNT(CASE WHEN phone IS NOT NULL THEN 1 END) as users_with_phone,
    COUNT(CASE WHEN full_name IS NOT NULL THEN 1 END) as users_with_name
FROM users;

-- 3. 查看最新的用户注册信息
SELECT 
    username,
    email,
    phone,
    full_name,
    created_at
FROM users 
WHERE created_at >= (CURRENT_DATE - INTERVAL '7 days')
ORDER BY created_at DESC;

-- 4. 删除指定用户（谨慎使用！）
-- 将下面的 '要删除的用户名' 替换为实际的用户名
-- DELETE FROM users WHERE username = '要删除的用户名';

-- 5. 删除所有普通用户（保留管理员账户）
-- DELETE FROM users WHERE role = 'USER';

-- 6. 删除没有手机号的测试用户
-- DELETE FROM users WHERE phone IS NULL AND role = 'USER';

-- 7. 清空所有用户数据（谨慎使用！）
-- DELETE FROM users;
-- 重置自增ID序列
-- ALTER SEQUENCE users_id_seq RESTART WITH 1;

-- 8. 查看银行卡数据（如果有）
SELECT * FROM bank_cards ORDER BY created_at DESC;

-- 9. 删除银行卡数据（如果需要）
-- DELETE FROM bank_cards;

-- 重要提醒：
-- 1. 在执行删除操作前，请先备份数据
-- 2. 确认要删除的用户名是否正确
-- 3. 建议先执行查询语句确认数据
-- 4. 删除操作不可逆，请谨慎操作