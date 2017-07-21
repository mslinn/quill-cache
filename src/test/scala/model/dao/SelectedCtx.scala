package model.dao

import model.PaymentMechanism

/** Define `SelectedCtx` for use with all DAOs; it could provide all implicit Decoder/Encoder/Mappers */
class SelectedCtx extends model.persistence.H2Ctx {
 // Cannot get this dang thing to work:
 // implicit val paymentMechanismEncoder = new EnumQuillEncoder[PaymentMechanism](ctx)

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
