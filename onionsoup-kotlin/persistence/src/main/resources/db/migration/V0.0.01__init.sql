drop table if exists loan_application_id;
create table loan_application_id
(
    id          varchar unique primary key,
    created_at  timestamp not null,
    modified_at timestamp not null
);

drop table if exists loan_application_customer;
create table loan_application_customer
(
    loan_application_id varchar unique primary key,
    first_name          varchar,
    second_name         varchar,
    birth_date          date,
    monthly_income      integer,
    street              text,
    zip_code            text,
    city                text,
    country             text,
    constraint fk_application_application__customer foreign key (loan_application_id) references loan_application_id (id)
);

create table loan_application_loan
(
    loan_application_id varchar unique primary key,
    amount              integer,
    duration            integer,
    interest_rate       integer,
    constraint fk_loan_application__loan foreign key (loan_application_id) references loan_application_id (id)
);

create table loan_application_property
(
    loan_application_id varchar unique primary key,
    value               integer,
    street              text,
    zip_code            text,
    city                text,
    country             text,
    constraint fk_loan_application__property foreign key (loan_application_id) references loan_application_id (id)
);
