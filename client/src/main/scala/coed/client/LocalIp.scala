package coed.client

import scala.util.Try

final case class IpAddress(addr: String) extends AnyVal

object LocalIp {

  def whatIsMyIp(toward: String): Option[IpAddress] = {
    import scala.sys.process._
    import scala.language.postfixOps

    validateIp(toward).flatMap(to => {
      val output = s"ip route get $toward" !!

      val ip = output.split(' ')(6)
      validateIp(ip)
    })
  }

  def validateIp(ip: String): Option[IpAddress] = {
    Try {
      ip.split('.').map(_.toInt).toList
    }.toOption.flatMap(bytes => {
      if (bytes.length == 4 && bytes.forall(x => 0 <= x && x <= 255)) {
        Some(IpAddress(ip))
      } else {
        None
      }
    })
  }
}