import remote.Remote

class Department(val name: String) extends Serializable {
  override def toString = name
}

object Department {
  def apply(name: String) = new Department(name)
}
