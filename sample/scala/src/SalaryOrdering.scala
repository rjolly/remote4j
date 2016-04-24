object SalaryOrdering extends Ordering[Employee] {
  def compare(x: Employee, y: Employee) = {
    if (x.salary < y.salary) -1
    else if (x.salary > y.salary) 1
    else x compare y
  }
}
