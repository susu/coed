package coed.common

import scala.util.Try

final case class IpAddress(addr: String) extends AnyVal

object IpAddress {
  def whatIsMyIp(toward: String): Option[IpAddress] = {
    import scala.sys.process._
    import scala.language.postfixOps

    validateIp(toward).flatMap(to => {
      val output = s"ip route get $toward" !!

      val words = output.split(' ')
      val ips = words.map(validateIp(_)).collect {case i@Some(_) => i}
      ips.last
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
