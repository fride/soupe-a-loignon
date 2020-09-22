package onionsoup

import arrow.core.extensions.list.foldable.foldLeft
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

class DraftViewPage(private val driver: WebDriver, private val baseUri: String) {
  val id = driver.currentUrl.split("/").last()

  val customerContents: Map<String, String?> =
    driver.findElements(By.className("customer-data")).foldLeft(mutableMapOf<String, String?>()) { acc, tr ->
      val name = tr.findElement(By.tagName("th")).text
      val value = tr.findElement(By.tagName("td")).text
      acc[name] = value?.trim()
      acc
    }

  fun fillInRandomStuff(): DraftIndexPage {
    print(driver.pageSource)
    val fillRandomStuffButton: WebElement? = driver.findElement(By.id("fillStuff"))
    fillRandomStuffButton?.submit()
    return DraftIndexPage(driver, baseUri)
  }

  fun submitLoanApplication(): DraftIndexPage {
    print(customerContents)
    driver.findElement(By.id("submitDraft")).submit()
    return DraftIndexPage(driver, baseUri)
  }

  companion object {
    fun WebDriver.openViewPage(baseUri: String, id: String): DraftViewPage {
      this.get("$baseUri/loan_application/draft/$id")
      return DraftViewPage(this, baseUri)
    }
  }
}
