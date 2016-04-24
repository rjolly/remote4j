import remote.Remote

class Employee(val name: String) extends Ordered[Employee] with Serializable {
  var department: Department = _
  var location: String = _
  var salary: Double = _
  var manager: Employee = _
  var job: String = _
  override def toString = name + " (" + department + ")"
  def compare(that: Employee) = this.name compare that.name
}

object Employee {
  def apply(name: String) = new Employee(name)
}
