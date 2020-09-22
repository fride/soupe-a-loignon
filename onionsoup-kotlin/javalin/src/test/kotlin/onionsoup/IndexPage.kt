package onionsoup

import arrow.core.extensions.list.foldable.foldLeft
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

class IndexPage(private val driver: WebDriver, private val baseUri: String) {
  private val loanApplications: List<WebElement> = driver.findElements(By.className("loan-application"))

  private val loanApplicationIds: Map<String, String> = loanApplications.foldLeft(mutableMapOf()) { acc, tr ->
    val href = tr.findElement(By.tagName("a")).getAttribute("href")
    acc[href.split("/").last()] = href
    acc
  }

  fun navigateToViewPage(id: String): DraftViewPage {
    val href = this.loanApplicationIds[id] ?: throw NoSuchElementException("No draft with id $id found")
    driver.get(href)
    return DraftViewPage(driver, baseUri)
  }
  companion object {
    fun open(driver: WebDriver, baseUri: String) : IndexPage {
      driver.get(baseUri + "/loan_application")
      return IndexPage(driver, baseUri)
    }
  }
}
