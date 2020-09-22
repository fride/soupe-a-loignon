create table operator
(
    op_login         text unique primary key,
    first_name       text    not null,
    last_name        text    not null,
    competence_level integer not null -- in cents.
);

create table loan_application_operator
(
    loan_application_id varchar unique primary key,
    operator            text not null,
    constraint fk_loan_application_operator__loan_application foreign key (loan_application_id) references loan_application_id (id),
    constraint loan_application_operator__operator foreign key (operator) references operator (op_login)
);
drop view if exists loan_application_details;

create view loan_application_details as
(
select loan_application_id.id          as id,
       loan_application_id.modified_at as modified_at,
       loan_application_id.created_at  as created_at,
       property.street                 as property_street,
       property.zip_code               as property_zip_code,
       property.city                   as property_city,
       property.country                as property_cuntry,
       property.value                  as property_value,
       loan.amount                     as loan_amount,
       loan.duration                   as loan_duration,
       loan.interest_rate              as loan_interest_rate,
       customer.first_name             as customer_first_name,
       customer.second_name            as customer_last_name,
       customer.birth_date             as customer_birth_date,
       customer.monthly_income         as customer_monthly_income,
       customer.street                 as customer_street,
       customer.zip_code               as customer_zip_code,
       customer.city                   as customer_city,
       customer.country                as customer_country,
       validation_errors.errors        as validation_errors,
       state.submitted_at              as submitted_at,
       state.checked_at                as checked_at,
       state.accepted_at               as accepted_at,
       state.rejected_at               as rejected_at,
       operator.operator               as operator,
       operator_details.first_name     as operator_first_name,
       operator_details.last_name     as operator_last_name
from loan_application_id
         left join loan_application_customer customer on loan_application_id.id = customer.loan_application_id
         left join loan_application_loan loan on loan_application_id.id = loan.loan_application_id
         left join loan_application_property property on loan_application_id.id = property.loan_application_id
         left join loan_application_validation_errors validation_errors
                   on loan_application_id.id = validation_errors.loan_application_id
         left join loan_application_state state on loan_application_id.id = state.loan_application_id
         left join loan_application_operator operator on loan_application_id.id = operator.loan_application_id
         left join operator operator_details on operator.operator = operator_details.op_login
    );

insert into operator (op_login, first_name, last_name, competence_level) values ('test1', 'some' ,'test user', 2000000);
insert into operator (op_login, first_name, last_name, competence_level) values ('gerald', 'Gerald', 'of Rivia', 2000004440)
