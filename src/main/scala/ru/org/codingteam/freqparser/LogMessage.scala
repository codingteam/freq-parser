package ru.org.codingteam.freqparser

import org.joda.time.LocalTime

case class LogMessage(time: LocalTime,
                      sender: String,
                      messageType: LogMessageType,
                      message: String)