package ru.org.codingteam.freqparser

import org.apache.commons.lang3.StringEscapeUtils
import org.joda.time.{LocalDate, LocalTime}

import scala.collection.mutable

object ParseHelpers {

  type RawMessage = (LocalTime, String, String, String)

  def extractRoomJid(content: String): Option[String] =
    "<a class=\"roomjid\".*>(.+?@.+?)</a>".r.findFirstMatchIn(content).map(_.group(1))

  def extractDate(content: String): Option[LocalDate] =
    "<div class=\"logdate\">(\\d{2}).(\\d{2}).(\\d{4})".r.findFirstMatchIn(content).map {
      m => new LocalDate(m.group(3).toInt, m.group(2).toInt, m.group(1).toInt)
    }

  def extractRawMessages(content: String): Iterator[RawMessage] = {
    val rawMessagePattern =
      "<a.*?>\\[(\\d{2}):(\\d{2}):(\\d{2})\\]</a> <font class=\"(.*?)\">(.*?)</font>(.*?)<br/>".r

    rawMessagePattern.findAllMatchIn(content).map(
      m => {
        val time = new LocalTime(m.group(1).toInt, m.group(2).toInt, m.group(3).toInt)
        val messageClass = m.group(4)
        val firstChunk = StringEscapeUtils.unescapeHtml4(m.group(5))
        val secondChunk = StringEscapeUtils.unescapeHtml4(m.group(6))
        (time, messageClass, firstChunk, secondChunk)
      }
    )
  }

  def extractMeMessage(participants: List[String], content: String): Option[(String, String)] =
    participants.map({
      p => s"\\* \\Q$p\\E (.*?)$$".r.findFirstMatchIn(content).map(m => (p, m.group(1)))
    }).find(_.isDefined).getOrElse(None)

  def constructLogMessage(rawMessage: RawMessage, participants: mutable.HashSet[String]): Option[LogMessage] =
    rawMessage match {
      case (time, "mn", RegexExtractors.RegularMessageNickname(sender), message) => {
        Some(LogMessage(time, sender, RegularMessageType, message.tail))
      }

      case (time, "mj", RegexExtractors.EnterMessageNickname(sender), "") => {
        Some(LogMessage(time, sender, EnterMessageType, ""))
      }

      case (time, "ml", RegexExtractors.LeaveMessage(sender, nullableReason), "") => {
        val reason = if (nullableReason == null) "" else nullableReason
        Some(LogMessage(time, sender, LeaveMessageType, s"User left: $reason"))
      }

      case (time, "ml", RegexExtractors.KickMessage(sender, nullableReason), "") => {
        val reason = if (nullableReason == null) "" else nullableReason
        Some(LogMessage(time, sender, LeaveMessageType, s"User kicked: $reason"))
      }

      case (time, "ml", RegexExtractors.BanMessage(sender, nullableReason), "") => {
        val reason = if (nullableReason == null) "" else nullableReason
        Some(LogMessage(time, sender, LeaveMessageType, s"User banned: $reason"))
      }

      case (time, "ml", RegexExtractors.RenameMessage(sender, newNick), "") => {
        participants.add(newNick)
        Some(LogMessage(time, sender, LeaveMessageType, s"renamed to $newNick"))
      }

      case (time, "mne", firstChunk, "") => extractMeMessage(participants.toList, firstChunk) match {
        case Some((sender, message)) =>
          Some(LogMessage(time, sender, RegularMessageType, s"/me $message"))
        case None => None
      }

      case (time, "roomcsubject", RegexExtractors.ChangeTopic(sender, topic), "") => {
        Some(LogMessage(time, sender, RegularMessageType, s"changed the topic: $topic"))
      }

      case _ => None
    }
}
