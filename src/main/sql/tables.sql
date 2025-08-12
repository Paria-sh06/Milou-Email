create table User(
    userId int primary key auto_increment,
    fullname nvarchar(250) not null,
    emailAddress nvarchar(250) not null unique,
    password nvarchar(250) not null
);