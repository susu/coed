package coed.common

import org.scalatest.{FreeSpec, Matchers}

class FrameSuite extends FreeSpec with Matchers {
  val testBufferText: String = (
    "123456789\n" +
    "23\n" +
    "3456789\n" +
    "456789101112\n" +
    "56789\n")

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
}
