import remote.Remote
import java.text.NumberFormat
import java.util.Collection;
import java.util.TreeSet;
import scala.language.implicitConversions
import scala.collection.convert.WrapAsScala.iterableAsScalaIterable

object Sample extends App {
  implicit val obj = Remote.lookup[Collection[Employee]]("obj")

  // Populate

  for (employees <- obj) yield {
  val accounting = Department("Accounting")
  val research = Department("Research")
  val sales = Department("Sales")
  employees.clear

  val clark = Employee("Clark")
  clark.department = accounting
  clark.location = "New York"
  clark.salary = 29400.0
  clark.job = "Manager"
  employees.add(clark)

  val king = Employee("King")
  king.department = accounting
  king.location = "New York"
  king.salary = 60000.0
  king.job = "President"
  employees.add(king)

  val miller = Employee("Miller")
  miller.department = accounting
  miller.location = "New York"
  miller.salary = 15600.0
  miller.job = "Clerk"
  employees.add(miller)

  val smith = Employee("Smith")
  smith.department = research
  smith.location = "New York"
  smith.salary = 11400.0
  smith.job = "Clerk"
  employees.add(smith)

  val adams = Employee("Adams")
  adams.department = research
  adams.location = "New York"
  adams.salary = 11400.0
  adams.job = "Clerk"
  employees.add(adams)

  val ford = Employee("Ford")
  ford.department = research
  ford.location = "New York"
  ford.salary = 36000.0
  ford.job = "Analyst"
  employees.add(ford)

  val scott = Employee("Scott")
  scott.department = research
  scott.location = "New York"
  scott.salary = 36000.0
  scott.job = "Analyst"
  employees.add(scott)

  val jones = Employee("Jones")
  jones.department = research
  jones.location = "New York"
  jones.salary = 35700.0
  jones.job = "Manager"
  employees.add(jones)

  val allen = Employee("Allen")
  allen.department = sales
  allen.location = "New York"
  allen.salary = 16800.0
  allen.job = "Salesman"
  employees.add(allen)

  val blake = Employee("Blake")
  blake.department = sales
  blake.location = "New York"
  blake.salary = 34200.0
  blake.job = "Manager"
  employees.add(blake)

  val martin = Employee("Martin")
  martin.department = sales
  martin.location = "New York"
  martin.salary = 16800.0
  martin.job = "Salesman"
  employees.add(martin)

  val james = Employee("James")
  james.department = sales
  james.location = "New York"
  james.salary = 11400.0
  james.job = "Clerk"
  employees.add(james)

  val turner = Employee("Turner")
  turner.department = sales
  turner.location = "New York"
  turner.salary = 16800.0
  turner.job = "Salesman"
  employees.add(turner)

  val ward = Employee("Ward")
  ward.department = sales
  ward.location = "New York"
  ward.salary = 16800.0
  ward.job = "Salesman"
  employees.add(ward)

  king.manager = king
  jones.manager = king
  scott.manager = jones
  adams.manager = scott
  ford.manager = jones
  smith.manager = ford
  blake.manager = king
  allen.manager = blake
  ward.manager = blake
  martin.manager = blake
  turner.manager = blake
  james.manager = turner
  clark.manager = king
  miller.manager = clark
  }

  // Query

  for (employees <- obj) yield {
  val format = NumberFormat.getCurrencyInstance

  val average = Employee("")
  val n = employees.size
  val s = employees.map(_.salary).sum
  average.salary = s / n

  for (e <- employees if e.salary < average.salary) println(e + " " + format.format(e.salary))

  val bySalary = new TreeSet(SalaryOrdering)
  bySalary.addAll(employees)

  println

  for (e <- bySalary.tailSet(average)) println(e + " " + format.format(e.salary))
  }
}
