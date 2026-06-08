--TABLE Users - таблица с минимальными данными пользователей.
create table Users (
	user_id serial primary key,
	login text unique not null,
	password text not null,
	role_id int references Roles(role_id) default 2
);

--TABLE Roles - Роли пользователей в системе
create table Roles (
	role_id serial primary key,
	role_name text not null
)
insert into Roles values (1, 'Admin'), (2, 'User');

--TABLE Files - таблица с хранением данных о файлах, которые храняться в S3 хранилище.
create table Files(
	file_id UUID primary key,
	owner_user_id int references Users(user_id) not null,
	filename text not null, 
	bucket varchar(64) not null,
	object_key text not null,
	content_type varchar(32) not null, --метаданные (MIME тип файла)
	size bigint not null, --размер в байтах
	created_at timestamptz not null, --дата создания
	is_public boolean not null default true
)

--TABLE file_activity_log - таблица логирования взаимодествия с файлами.
--UPLOAD — загрузка файла в S3
--DOWNLOAD — скачивание файла
--DELETE — удаление файла
--UPDATE — замена/обновление файла
--SHARE — создание доступа/ссылки
--VIEW — просмотр
create table file_activity_log (
	id serial primary key,
	file_id uuid references Files(file_id) on delete set null,
	user_id int references Users(user_id) on delete cascade,
	file_operation varchar(8) not null,
	occurred_at timestamptz not null
)