create table User(
    userId int primary key auto_increment,
    fullname nvarchar(250) not null,
    emailAddress nvarchar(250) not null unique,
    password nvarchar(250) not null
);

create table Emails(
    emailId int primary key auto_increment,
    uniquecode nvarchar(6) not null unique,
    sender_id int not null,
    title TINYTEXT not null,
    messageBody TEXT,
    creation_time datetime not null,
    foreign key (sender_id) references User(userId)
);

create table Email_Recipients(
    Id int primary key auto_increment,
    recipient_id int not null,
    email_id int not null,
    is_read boolean default false,
    foreign key (recipient_id) references User(userId),
    foreign key (email_id) references Emails(emailId)
);
drop table Email_Recipients, Emails;
show tables;