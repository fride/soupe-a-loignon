create table loan_application_validation_errors
(
    loan_application_id varchar unique primary key,
    errors              jsonb not null
);

drop view if exists loan_application_details;

create view loan_application_details as
(
select loan_application_id.id,
       property.street          as property_street,
       property.zip_code        as property_zip_code,
       property.city            as property_city,
       property.country         as property_cuntry,
       property.value           as property_value,
       loan.amount              as loan_amount,
       loan.duration            as loan_duration,
       loan.interest_rate       as loan_interest_rate,
       customer.first_name      as customer_first_name,
       customer.second_name     as customer_last_name,
       customer.birth_date      as customer_birth_date,
       customer.monthly_income  as customer_monthly_income,
       customer.street          as customer_street,
       customer.zip_code        as customer_zip_code,
       customer.city            as customer_city,
       customer.country         as customer_country,
       validation_errors.errors as validation_errors

from loan_application_id
         left join loan_application_customer customer on loan_application_id.id = customer.loan_application_id
         left join loan_application_loan loan on loan_application_id.id = loan.loan_application_id
         left join loan_application_property property on loan_application_id.id = property.loan_application_id
         left join loan_application_validation_errors validation_errors
                   on loan_application_id.id = validation_errors.loan_application_id
    )
