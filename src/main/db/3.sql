use chat;

select * from messages where messages.user_id = (select users.id from users where users.name = 'Gennady')
and date(messages.date) = '2015-05-02';
