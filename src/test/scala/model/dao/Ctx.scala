package model.dao

import model.PaymentMechanism
import model.persistence.{H2Ctx, QuillCacheImplicits}

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
}
