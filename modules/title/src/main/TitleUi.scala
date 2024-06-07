package lila.title
package ui

import play.api.data.Form
import play.api.libs.json.*
import chess.PlayerTitle

import lila.ui.*
import ScalatagsTemplate.{ *, given }
import lila.core.id.TitleRequestId
import lila.core.id.ImageId

final class TitleUi(helpers: Helpers)(picfitUrl: lila.core.misc.PicfitUrl):
  import helpers.{ *, given }

  private def layout(title: String = "Title verification")(using Context) =
    Page(title)
      .css("bits.titleRequest")
      .wrap: body =>
        main(cls := "page-small box box-pad page")(body)

  def index(title: String, intro: Frag)(using Context) =
    layout(title).css("bits.page"):
      frag(
        intro,
        br,
        br,
        br,
        div(style := "text-align: center;")(
          a(cls := "button button-fat", href := routes.TitleVerify.form)("Verify your title")
        )
      )

  def create(form: Form[TitleRequest.FormData])(using Context) =
    layout():
      frag(
        h1(cls := "box__top")("Verify your title"),
        dataForm(form)
      )

  def edit(form: Form[TitleRequest.FormData], req: TitleRequest)(using Context) =
    val title = "Your title verification"
    layout(title)
      .js(EsmInit("bits.titleRequest")):
        frag(
          h1(cls := "box__top")(title),
          standardFlash,
          div(cls := "title__images")(selfieImage(req)),
          dataForm(form)
        )

  private def dataForm(form: Form[TitleRequest.FormData])(using Context) =
    postForm(cls := "form3", action := routes.TitleVerify.create)(
      form3.globalError(form),
      form3.group(
        form("realName"),
        "Full real name",
        half = true
      )(form3.input(_)(autofocus)),
      form3.group(
        form("title"),
        "Title"
      ): field =>
        form3.select(
          field,
          availableTitles.map(t => t -> t.value),
          default = "Select your title".some
        ),
      form3.split(
        form3.group(
          form("fideId"),
          "Your FIDE ID or profile URL",
          help = frag("If you have one.").some,
          half = true
        )(form3.input(_)),
        form3.group(
          form("federationUrl"),
          "Your national federation profile URL",
          help = frag("If you have one.").some,
          half = true
        )(form3.input(_))
      ),
      form3.split(
        form3.checkbox(
          form("public").copy(value = "true".some),
          frag("Publish my title"),
          help = frag(
            "Recommended. Your title is visible on your profile page. Your real name is NOT published."
          ).some,
          half = true
        ),
        form3.checkbox(
          form("coach"),
          frag("Create a coach profile"),
          help = frag(
            "Offer your services as a coach, and appear in the ",
            a(href := routes.Coach.all())("coach directory"),
            "."
          ).some,
          half = true
        )
      ),
      form3.group(
        form("comment"),
        "Comment",
        help = frag("Optional additional information for the moderators.").some,
        half = true
      )(form3.textarea(_)(rows := 8)),
      form3.action(form3.submit("Send"))
    )

  private def selfieImage(t: TitleRequest)(using ctx: Context) =
    val image = t.selfie
    div(cls := "title-image-edit", data("post-url") := routes.TitleVerify.image(t.id, "selfie"))(
      thumbnail(image)(
        cls               := List("drop-target" -> true, "user-image" -> image.isDefined),
        attr("draggable") := "true"
      ),
      div(
        p("Upload a photo of you holding your ID document."),
        p(trans.streamer.maxSize(s"${lila.memo.PicfitApi.uploadMaxMb}MB.")),
        form3.file.selectImage()
      )
    )

  private object thumbnail:
    def apply(image: Option[ImageId]): Tag =
      image.fold(fallback): id =>
        img(cls := "relay-image", src := url(id))
    def fallback         = iconTag(Icon.UploadCloud)(cls := "title-image--fallback")
    def url(id: ImageId) = picfitUrl.thumbnail(id, 800, 800)
