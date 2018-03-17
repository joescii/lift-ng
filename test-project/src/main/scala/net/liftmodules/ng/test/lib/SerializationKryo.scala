package net.liftmodules.ng.test.lib

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import net.liftmodules.cluster.{LiftCluster, LiftClusterConfig}
import net.liftmodules.cluster.kryo.{KryoSerializable, KryoSerializableLiftSession}
import net.liftweb.common.Box
import net.liftweb.http.{LiftResponse, LiftSession, Req}


object LiftSessionSerialization {
  def init(): Unit = {
    // Enable clustering so all of our serialization hacks will be wired up
    LiftCluster.init(LiftClusterConfig(KryoSerializableLiftSession.serializer))

    // Anytime a request has completed, make sure the session is still serializable
//    LiftSession.onEndServicing = LiftSession.onEndServicing :+ {(s: LiftSession, _: Req, _: Box[LiftResponse]) =>
//      println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
//      roundTrip(s.httpSession)
//      ()
//    }
  }

  private [this] def roundTrip[T](in :T): T = deserialize(serialize(in)) // deserialize[KryoSerializable[T]](serialize(KryoSerializable(in))).obj

  private [this] def serialize[T](in: T): Array[Byte] = {
    val bos = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(bos)
    oos.writeObject(in)
    oos.flush()
    bos.toByteArray()
  }

  private [this] def deserialize[T](in: Array[Byte]): T = {
    val bis = new ByteArrayInputStream(in)
    val ois = new ObjectInputStream(bis)
    ois.readObject.asInstanceOf[T]
  }

}
