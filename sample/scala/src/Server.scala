import remote.Remote
import java.io.{File, FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}
import java.util.{Collection, ArrayList}

object Server extends App {
  val file = new File("remote.ser")
  val employees = if (file.exists) {
    println("reading state")
    val is = new ObjectInputStream(new FileInputStream(file))
    val obj = is.readObject()
    is.close
    obj.asInstanceOf[Collection[Employee]]
  } else new ArrayList[Employee]
  Remote.rebind("obj", employees)
  println("obj bound in registry")
  Runtime.getRuntime().addShutdownHook(new Thread {
    override def run = {
      println("writing state")
      val os = new ObjectOutputStream(new FileOutputStream(file))
      os.writeObject(employees)
      os.close
    }
  })
}
