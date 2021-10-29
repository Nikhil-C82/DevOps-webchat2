use chat;

select * from messages where messages.user_id = (select users.id from users where users.name = 'Shyshpanchik')
and messages.text like "%hello%";
