package coed.common

import org.scalatest.{FreeSpec, Matchers}

class FrameSuite extends FreeSpec with Matchers {
  val testBufferText: String =  "123456789\n" +
                                "23\n" +
                                "3456789\n" +
                                "456789101112\n" +
                                "56789\n"

  "calculating visible lines in buffer" - {
    "when offset is (0, 0) and whole frame fits in buffer" in {
      val testFrame = Frame (bufferText = testBufferText,
                             bufferOffset = (0, 0),
                             cursorPosition = FrameCoords(1, 1),
                             frameHeight = 3,
                             frameWidth = 5)

      val expectedLines = Seq("12345", "23", "34567")

      testFrame.visibleLines shouldBe expectedLines
    }

    "when offset is not zero and whole frame fits in buffer" in {
      val testFrame = Frame (bufferText = testBufferText,
        bufferOffset = (2, 1),
        cursorPosition = FrameCoords(1, 1),
        frameHeight = 3,
        frameWidth = 5)

      val expectedLines = Seq("", "56789", "67891")

      testFrame.visibleLines shouldBe expectedLines
    }

    "when frame is higher than the number of lines in buffer" in {
      val testFrame = Frame (bufferText = testBufferText,
        bufferOffset = (0, 0),
        cursorPosition = FrameCoords(1, 1),
        frameHeight = 6,
        frameWidth = 1)

      val expectedLines = Seq("1", "2", "3", "4", "5", Frame.EMPTY_LINE_REPRESENTATION)

      testFrame.visibleLines shouldBe expectedLines
    }
  }

  "cursor movement" - {
    "up" - {
      "should return same frame when [Y buffer offset: 0, Y cursor pos: 1 (top of frame)]" in {
        val frame = Frame (bufferText = testBufferText, bufferOffset = (3, 0), cursorPosition = FrameCoords(3, 1))
        val newFrame = frame.moveCursorUp
        newFrame shouldBe frame
      }

      "only change should be 1 less Y buffer offset when [Y buffer offset: >0, Y cursor pos: 1]" in {
        val frame = Frame (bufferText = testBufferText, bufferOffset = (3, 1), cursorPosition = FrameCoords(3, 1))
        val newFrame = frame.moveCursorUp
        newFrame.bufferOffset._1 shouldBe frame.bufferOffset._1
        newFrame.bufferOffset._2 shouldBe frame.bufferOffset._2 - 1
        newFrame.cursorPosition shouldBe frame.cursorPosition
      }

      "only change should be 1 less Y cursor pos when [Y buffer offset: >0, Y cursor pos: >1]" in {
        val frame = Frame (bufferText = testBufferText, bufferOffset = (3, 1), cursorPosition = FrameCoords(1, 2))
        val newFrame = frame.moveCursorUp
        newFrame.bufferOffset shouldBe frame.bufferOffset
        newFrame.cursorPosition.at shouldBe frame.cursorPosition.at
        newFrame.cursorPosition.line shouldBe frame.cursorPosition.line - 1
      }
    }

    "left" - {
      "should return same frame when [X buffer offset: 0, X cursor pos: 1 (left side of frame)]" in {
        val frame = Frame (bufferText = testBufferText, bufferOffset = (0, 3), cursorPosition = FrameCoords(1, 3))
        val newFrame = frame.moveCursorLeft
        newFrame shouldBe frame
      }

      "only change should be 1 less X buffer offset when [X buffer offset: >0, X cursor pos: 1]" in {
        val frame = Frame (bufferText = testBufferText, bufferOffset = (1, 3), cursorPosition = FrameCoords(1, 3))
        val newFrame = frame.moveCursorLeft
        newFrame.bufferOffset._1 shouldBe frame.bufferOffset._1 - 1
        newFrame.bufferOffset._2 shouldBe frame.bufferOffset._2
        newFrame.cursorPosition shouldBe frame.cursorPosition
      }

      "only change should be 1 less X cursor pos when [X buffer offset: >0, X cursor pos: >1]" in {
        val frame = Frame (bufferText = testBufferText, bufferOffset = (1, 3), cursorPosition = FrameCoords(2, 2))
        val newFrame = frame.moveCursorLeft
        newFrame.bufferOffset shouldBe frame.bufferOffset
        newFrame.cursorPosition.at shouldBe frame.cursorPosition.at - 1
        newFrame.cursorPosition.line shouldBe frame.cursorPosition.line
      }
    }

    "down" - {
      "should return same frame when [Y buffer offset: buffer height - frame height, Y cursor pos: frame height]" in {
        val testBufferHeight = testBufferText.lines.size
        val testFrameHeight = 3
        val frame = Frame (bufferText = testBufferText,
                           frameHeight = testFrameHeight,
                           bufferOffset = (3, testBufferHeight - testFrameHeight),
                           cursorPosition = FrameCoords(3, testFrameHeight))

        val newFrame = frame.moveCursorDown
        newFrame shouldBe frame
      }

      "only change should be 1 more Y buffer offset when [Y buffer offset: > (buffer height - frame height), Y cursor pos: frame height]" in {
        val testBufferHeight = testBufferText.lines.size
        val testFrameHeight = 3
        val frame = Frame (bufferText = testBufferText,
                           frameHeight = testFrameHeight,
                           bufferOffset = (3, (testBufferHeight - testFrameHeight) - 1),
                           cursorPosition = FrameCoords(3, testFrameHeight))

        val newFrame = frame.moveCursorDown
        newFrame.bufferOffset._1 shouldBe frame.bufferOffset._1
        newFrame.bufferOffset._2 shouldBe frame.bufferOffset._2 + 1
        newFrame.cursorPosition shouldBe frame.cursorPosition
      }

      "only change should be 1 more Y cursor pos when [Y cursor pos: < frame height]" in {
        val frame = Frame (bufferText = testBufferText, bufferOffset = (3, 1), cursorPosition = FrameCoords(1, 2))
        val newFrame = frame.moveCursorDown
        newFrame.bufferOffset shouldBe frame.bufferOffset
        newFrame.cursorPosition.at shouldBe frame.cursorPosition.at
        newFrame.cursorPosition.line shouldBe frame.cursorPosition.line + 1
      }
    }
  }

  "right" - {
    val testText: String = "123456789\n" +
                           "2345678"

    val testFrameWidth = 3
    val currentLineNumberInFrameCoords = 2
    val currentLine = testText.lines.toList(currentLineNumberInFrameCoords - 1)

    "should return same frame when [current line length: X buffer offset + X cursor position, X cursor pos: frame width]" in {
      val frame = Frame (bufferText = testText,
        frameWidth = testFrameWidth,
        bufferOffset = (currentLine.size - testFrameWidth, 0),
        cursorPosition = FrameCoords(testFrameWidth, currentLineNumberInFrameCoords))

      val newFrame = frame.moveCursorRight
      newFrame shouldBe frame
    }

    "only change should be 1 more X buffer offset when [current line length: > (X buffer offset + X cursor position), X cursor pos: frame height]" in {
      val frame = Frame (bufferText = testText,
        frameWidth = testFrameWidth,
        bufferOffset = (currentLine.size - testFrameWidth - 1, 0),
        cursorPosition = FrameCoords(testFrameWidth, currentLineNumberInFrameCoords))

      val newFrame = frame.moveCursorRight
      newFrame.bufferOffset._1 shouldBe frame.bufferOffset._1 + 1
      newFrame.bufferOffset._2 shouldBe frame.bufferOffset._2
      newFrame.cursorPosition shouldBe frame.cursorPosition
    }

    "cursor is in middle of frame and visible line is shorter than frame width and cursor is at the end of line" in {
      val txt = "new\n" +
                "ne\n" +
                "new"

      val frame = Frame (bufferText = txt, bufferOffset = (0, 0), cursorPosition = FrameCoords(2, 2))
      val newFrame = frame.moveCursorRight
      newFrame shouldBe frame
    }

    "cursor is in middle of frame and visible line is shorter than frame width and cursor is not at the end of line" in {
      val txt = "new\n" +
                "ne\n" +
                "new"

      val frame = Frame (bufferText = txt, bufferOffset = (0, 0), cursorPosition = FrameCoords(1, 2))
      val newFrame = frame.moveCursorRight
      newFrame.bufferOffset shouldBe frame.bufferOffset
      newFrame.cursorPosition.at shouldBe frame.cursorPosition.at + 1
      newFrame.cursorPosition.line shouldBe frame.cursorPosition.line
    }
  }
}
