package onionsoup.loanapplication.readmodel

import arrow.core.Option
import arrow.fx.IO

// READ MODEL DEPENDENCIES
typealias LoadDashboard = (DashboardQuery) -> IO<LoanApplicationDashboardDto>
typealias FindLoanApplicationDetails = (String) -> IO<Option<LoanApplicationDetails>>
