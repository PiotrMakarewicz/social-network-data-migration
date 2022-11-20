CREATE TABLE users (id INT PRIMARY KEY, firstname VARCHAR(255), lastname VARCHAR(255))
CREATE TABLE posts (id INT PRIMARY KEY, author_id INT REFERENCES users(id), content VARCHAR(255))
CREATE TABLE user_likes_post (user_id INT REFERENCES users(id), post_id INT REFERENCES posts(id))

INSERT INTO users (id, firstname, lastname) VALUES (1, 'John', 'Smith')
INSERT INTO users (id, firstname, lastname) VALUES (2, 'Adam', 'Jackson')

INSERT INTO posts (id, author_id, content) VALUES (1, 1, 'Lorem ipsum.')
INSERT INTO posts (id, author_id, content) VALUES (2, 1, 'Dolor sit amet.')
INSERT INTO posts (id, author_id, content) VALUES (3, 2, 'Consectetur adipiscing elit.')

INSERT INTO user_likes_post(user_id, post_id) VALUES (1, 2)
INSERT INTO user_likes_post(user_id, post_id) VALUES (1, 3)
INSERT INTO user_likes_post(user_id, post_id) VALUES (2, 1)
