package model.dao

import model.PaymentMechanism
import model.persistence.Types.{IdOptionLong, OptionLong}
import model.persistence.{H2Ctx, Id, QuillCacheImplicits}

/** Define `Ctx` for use with all DAOs; it could provide all implicit Decoder/Encoder/Mappers */
case object Ctx extends H2Ctx with QuillCacheImplicits {
  // This does not work yet:
//  implicit val paymentMechanismEncoder = new model.persistence.EnumQuillEncoder[PaymentMechanism](Ctx)

  // So use the more verbose mechanism:
  implicit val encodePaymentMechanism: MappedEncoding[PaymentMechanism, String] =
     MappedEncoding[PaymentMechanism, String](_.name)

  implicit val decodePaymentMechanism: MappedEncoding[String, PaymentMechanism] =
    MappedEncoding[String, PaymentMechanism](PaymentMechanism.valueOf)


  implicit val encodeListPaymentMechanism: MappedEncoding[List[PaymentMechanism], String] =
    MappedEncoding[List[PaymentMechanism], String](_.map(_.name).mkString(","))

 implicit val decodeListPaymentMechanism: MappedEncoding[String, List[PaymentMechanism]] =
   MappedEncoding[String, List[PaymentMechanism]] { x =>
     val string = x.trim
     if (string.isEmpty) Nil
     else string.split(",").toList.map(PaymentMechanism.valueOf)
   }


  /*implicit val encodeIdOptionLongToListIntToArrayByte: MappedEncoding[IdOptionLongToListInt, Array[Byte]] =
    MappedEncoding[IdOptionLongToListInt, Array[Byte]] {
      _.map { case (key, values) => s"$key->${ values.mkString(",") }" }
       .mkString(";")
    }

  implicit val decodeIdOptionLongToListIntToArrayByte: MappedEncoding[Array[Byte], IdOptionLongToListInt] =
    MappedEncoding[Array[Byte], IdOptionLongToListInt] { map =>
      if (map.length==0) Map.empty else {
        val arrayOfTuples: Array[(Id[Option[Long]], List[Index])] = for {
          token <- map.split(";")
          Array(key, values) = token.split("->")
        } yield Id(Option(key.toLong)) -> values.split(",").map(_.toInt).toList
        arrayOfTuples.toMap
      }
    }*/
}
