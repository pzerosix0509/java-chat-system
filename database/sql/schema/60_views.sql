CREATE OR REPLACE VIEW v_user_friend_counts AS
SELECT
    u.user_id,
    COALESCE(d.direct_friends_count, 0) AS direct_friends_count,
    COALESCE(f.friends_of_friends_count, 0) AS friends_of_friends_count,
    COALESCE(d.direct_friends_count, 0) + COALESCE(f.friends_of_friends_count, 0) AS network_size_including_friends_of_friends
FROM users u
LEFT JOIN (
    SELECT edge.user_id, COUNT(*) AS direct_friends_count
    FROM (
        SELECT user_low_id AS user_id, user_high_id AS friend_id FROM friendships
        UNION ALL
        SELECT user_high_id AS user_id, user_low_id AS friend_id FROM friendships
    ) edge
    GROUP BY edge.user_id
) d ON d.user_id = u.user_id
LEFT JOIN (
    SELECT first_edge.user_id, COUNT(DISTINCT second_edge.friend_id) AS friends_of_friends_count
    FROM (
        SELECT user_low_id AS user_id, user_high_id AS friend_id FROM friendships
        UNION ALL
        SELECT user_high_id AS user_id, user_low_id AS friend_id FROM friendships
    ) first_edge
    JOIN (
        SELECT user_low_id AS user_id, user_high_id AS friend_id FROM friendships
        UNION ALL
        SELECT user_high_id AS user_id, user_low_id AS friend_id FROM friendships
    ) second_edge ON second_edge.user_id = first_edge.friend_id
    LEFT JOIN (
        SELECT user_low_id AS user_id, user_high_id AS friend_id FROM friendships
        UNION ALL
        SELECT user_high_id AS user_id, user_low_id AS friend_id FROM friendships
    ) direct_edge ON direct_edge.user_id = first_edge.user_id AND direct_edge.friend_id = second_edge.friend_id
    WHERE second_edge.friend_id <> first_edge.user_id AND direct_edge.friend_id IS NULL
    GROUP BY first_edge.user_id
) f ON f.user_id = u.user_id
WHERE u.status <> 'DELETED';
