package onionsoup.loanapplication

import arrow.core.Option
import arrow.fx.IO
import core.types.CrudOperation
import core.types.ModelUpdater

// Model updates
typealias PersistLoanApplicationCreated = ModelUpdater<LoanApplicationCreated>
typealias PersistLoanApplicationDraftChanged = ModelUpdater<LoanApplicationDraftUpdated>
typealias PersistLoanApplicationSubmitted = ModelUpdater<LoanApplicationSubmitted>
typealias PersistLoanApplicationScored = ModelUpdater<LoanApplicationScored>

// CRUD Operations
typealias CreateOperator = CrudOperation<Operator>
typealias UpdateOperator = CrudOperation<Operator>
typealias DeleteOperator = CrudOperation<String>

// Queries
typealias FindLoanApplicationDraft = (String) -> IO<Option<LoanApplicationDraft>>
typealias FindLoanApplication = (String) -> IO<Option<LoanApplication>>
typealias FindOperator = (String) -> IO<Option<Operator>>
typealias GetOperator = (String) -> IO<Option<Operator>>
typealias GetAllOperators = (Int, Int) -> IO<List<Operator>>

typealias GetDebtorState = (Customer) -> IO<DebtorState>
