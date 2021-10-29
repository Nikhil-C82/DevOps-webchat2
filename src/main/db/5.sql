use chat;

select users.* from users left join messages on messages.user_id = users.id
group by users.name having count(messages.id) > 3;
