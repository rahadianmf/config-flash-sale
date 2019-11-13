(ns znet.web-app.client.pages.config-flash-sale
  (:require-macros [znet.web-app.client.log.macros :refer [log print println]])
  (:refer-clojure :exclude [print println])
  (:require
   [goog.dom :as gdom]
   [goog.dom.classlist :as classlist]
   [goog.dom.xml :as xml]
   [goog.object :as gobj]
   [goog.labs.userAgent.platform :as userAgent.platform]
   [goog.net.cookies]
   [clojure.string :as string]
   [goog.labs.net.xhr :as xhr]
   [promesa.core :as p]
   [znet.web-app.client.user :as client.user]
   [goog.string.Const]
   )
  (:import (goog.html TrustedResourceUrl)
           goog.string.Const
           (goog.date UtcDateTime
                      DateTime)
           (goog.net XhrIo
                     EventType)))

(defn config-flash-sale
  ""
  [{:keys [state] :as page}]
  (let [current-head-url (gobj/get (gobj/get js/window "location") "href")
        current-url (gobj/get (gobj/get js/window "location") "pathname")
        url (if (string/includes? current-head-url "zenius.net")
            "https://raw.githubusercontent.com/rahadianmf/config-flash-sale/master/config.json"
            "https://raw.githubusercontent.com/rahadianmf/config-flash-sale/master/config-local.json"
            )
        promise (xhr/getJson url)
        close-elem    (js/document.querySelector "#fs-close")
        fsd           (js/document.querySelector "#flash-sale")
        img-desktop   (js/document.querySelector "#flash-sale-img-desktop")
        img-mobile    (js/document.querySelector "#flash-sale-img-mobile")
        link-desktop   (js/document.querySelector "#cta-url-desktop")
        link-mobile    (js/document.querySelector "#cta-url-mobile")
        user (get-in @state (get @state :current-user)) ;;get data through state. default.
        guest? (-> user :znet.web-app.user/roles :znet.web-app.user.role/guest)
        regular? (-> user :znet.web-app.user/roles :znet.web-app.user.role/member)
        premium? (-> user :znet.web-app.user/roles :znet.web-app.user.role/premium-member)
        success-cb-fn (fn [value] 
                        (let [status (gobj/getValueByKeys value "status")
                              startDate (string/split (gobj/getValueByKeys value "startDate") #" ")
                              startDateText (str (int (get startDate 2)) "-" (+ 1 (int (get startDate 1))) "-" (int (get startDate 0)))
                              endDate (string/split (gobj/getValueByKeys value "endDate") #" ")
                              campaignStart (DateTime. (int (get startDate 0)) (int (get startDate 1)) (int (get startDate 2)) (int (get startDate 3)) (int (get startDate 4)) (int (get startDate 5)))
                              campaignEnd (DateTime. (int (get endDate 0)) (int (get endDate 1)) (int (get endDate 2)) (int (get endDate 3)) (int (get endDate 4)) (int (get endDate 5)))
                              campaignDateStartGetTime (.getTime campaignStart)
                              campaignDateEndGetTime (.getTime campaignEnd)
                              dateNow (.getTime (js/Date.))]

                        ; flash sale checking status and actions
                        (when (= (gobj/getValueByKeys value "status") "true")
                          (if (< (- campaignDateStartGetTime dateNow) 0)
                            (do
                              (if (< (- campaignDateEndGetTime dateNow) 0)
                                (do
                                  (log :status "finished")
                                  (classlist/add fsd "dn"))
                                (do
                                  (log :status "campaign started.")
                                  (if (and (string/includes? current-head-url "zenius.net") (not= (gobj/getValueByKeys value "prod") true))
                                    (do 
                                      (log :prodstatus "prod status is not activated yet.")
                                      (classlist/add fsd "dn"))
                                    (cond 
                                      (userAgent.platform/isAndroid) 
                                        (do 
                                          (cond 
                                            (= (gobj/getValueByKeys value #js["mobileRole" "inactive"]) true)
                                              (classlist/add fsd "dn")
                                            (and guest? (= (gobj/getValueByKeys value #js["mobileRole" "guest"]) true))
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["mobileImg" "guest"]) "")
                                                  (classlist/add fsd "dn")
                                                  (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["mobileImg" "guest"]))) 
                                                (classlist/add fsd "db"))
                                            (and regular? (= (gobj/getValueByKeys value #js["mobileRole" "member"]) true))
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["mobileImg" "member"]) "")
                                                  (classlist/add fsd "dn")
                                                  (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["mobileImg" "member"]))) 
                                                (classlist/add fsd "db"))
                                            (and premium? (= (gobj/getValueByKeys value #js["mobileRole" "premium"]) true))
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["mobileImg" "premium"]) "")
                                                  (classlist/add fsd "dn")
                                                  (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["mobileImg" "premium"]))) 
                                                (classlist/add fsd "db"))
                                            (= (gobj/getValueByKeys value #js["mobileRole" "all"]) true)
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["mobileImg" "all"]) "")
                                                  (classlist/add fsd "dn")
                                                  (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["mobileImg" "all"]))) 
                                                (classlist/add fsd "db"))
                                            :else 
                                              (do 
                                                (log :role "none")
                                                (classlist/add fsd "dn")))
                                          (cond 
                                            (= current-url "/")
                                              (if (not= (gobj/getValueByKeys value #js["mobileUrl" "home"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["mobileUrl" "home"])))
                                            (= current-url "/membership")
                                              (if (not= (gobj/getValueByKeys value #js["mobileUrl" "membership"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["mobileUrl" "membership"])))
                                            (string/includes? current-url "/prologmateri")
                                              (if (not= (gobj/getValueByKeys value #js["mobileUrl" "prologmateri"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["mobileUrl" "prologmateri"])))
                                            (string/includes? current-url "/cg")
                                              (if (not= (gobj/getValueByKeys value #js["mobileUrl" "cg"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["mobileUrl" "cg"])))
                                            (= current-url "/download-soal")
                                              (if (not= (gobj/getValueByKeys value #js["mobileUrl" "downloadSoal"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["mobileUrl" "downloadSoal"])))
                                            :else (log :url "none"))
                                        )
                                      (userAgent.platform/isMacintosh) 
                                        (do 
                                          (cond 
                                            (= (gobj/getValueByKeys value #js["macintoshRole" "inactive"]) true)
                                              (classlist/add fsd "dn")
                                            (and guest? (= (gobj/getValueByKeys value #js["macintoshRole" "guest"]) true))
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["macintoshImg" "guest"]) "")
                                                  (classlist/add fsd "dn")
                                                  (.setAttribute img-desktop "src" (gobj/getValueByKeys value #js["macintoshImg" "guest"]))) 
                                                (classlist/add fsd "db"))
                                            (and regular? (= (gobj/getValueByKeys value #js["macintoshRole" "member"]) true))
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["macintoshImg" "member"]) "")
                                                  (classlist/add fsd "dn")
                                                  (.setAttribute img-desktop "src" (gobj/getValueByKeys value #js["macintoshImg" "member"]))) 
                                                (classlist/add fsd "db"))
                                            (and premium? (= (gobj/getValueByKeys value #js["macintoshRole" "premium"]) true))
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["macintoshImg" "premium"]) "")
                                                  (classlist/add fsd "dn")
                                                  (.setAttribute img-desktop "src" (gobj/getValueByKeys value #js["macintoshImg" "premium"]))) 
                                                (classlist/add fsd "db"))
                                            (= (gobj/getValueByKeys value #js["macintoshRole" "all"]) true)
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["macintoshImg" "all"]) "")
                                                  (classlist/add fsd "dn")
                                                  (.setAttribute img-desktop "src" (gobj/getValueByKeys value #js["macintoshImg" "all"]))) 
                                                (classlist/add fsd "db"))
                                            :else 
                                              (do 
                                                (log :role "none")
                                                (classlist/add fsd "dn")))
                                          (cond 
                                            (= current-url "/")
                                              (if (not= (gobj/getValueByKeys value #js["macintoshUrl" "home"]) "") (.setAttribute link-desktop "href" (gobj/getValueByKeys value #js["macintoshUrl" "home"])))
                                            (= current-url "/membership")
                                              (if (not= (gobj/getValueByKeys value #js["macintoshUrl" "membership"]) "") (.setAttribute link-desktop "href" (gobj/getValueByKeys value #js["macintoshUrl" "membership"])))
                                            (string/includes? current-url "/prologmateri")
                                              (if (not= (gobj/getValueByKeys value #js["macintoshUrl" "prologmateri"]) "") (.setAttribute link-desktop "href" (gobj/getValueByKeys value #js["macintoshUrl" "prologmateri"])))
                                            (string/includes? current-url "/cg")
                                              (if (not= (gobj/getValueByKeys value #js["macintoshUrl" "cg"]) "") (.setAttribute link-desktop "href" (gobj/getValueByKeys value #js["macintoshUrl" "cg"])))
                                            (= current-url "/download-soal")
                                              (if (not= (gobj/getValueByKeys value #js["macintoshUrl" "downloadSoal"]) "") (.setAttribute link-desktop "href" (gobj/getValueByKeys value #js["macintoshUrl" "downloadSoal"])))
                                            :else (log :url "none"))
                                        )
                                      (userAgent.platform/isIphone) 
                                        (do 
                                          (cond 
                                            (= (gobj/getValueByKeys value #js["iphoneRole" "inactive"]) true)
                                              (classlist/add fsd "dn")
                                            (and guest? (= (gobj/getValueByKeys value #js["iphoneRole" "guest"]) true))
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["iphoneImg" "guest"]) "")
                                                  (classlist/add fsd "dn")
                                                  (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["iphoneImg" "guest"])))
                                                (classlist/add fsd "db"))
                                            (and regular? (= (gobj/getValueByKeys value #js["iphoneRole" "member"]) true))
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["iphoneImg" "member"]) "")
                                                  (classlist/add fsd "dn")mobile
                                                  (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["iphoneImg" "member"])))
                                                (classlist/add fsd "db"))
                                            (and premium? (= (gobj/getValueByKeys value #js["iphoneRole" "premium"]) true))
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["iphoneImg" "premium"]) "")
                                                  (classlist/add fsd "dn")
                                                  (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["iphoneImg" "premium"]))) 
                                                (classlist/add fsd "db"))
                                            (= (gobj/getValueByKeys value #js["iphoneRole" "all"]) true)
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["iphoneImg" "all"]) "")
                                                  (classlist/add fsd "dn")
                                                  (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["iphoneImg" "all"]))) 
                                                (classlist/add fsd "db"))
                                            :else 
                                              (do 
                                                (log :role "none")
                                                (classlist/add fsd "dn")))
                                          (cond 
                                            (= current-url "/")
                                              (if (not= (gobj/getValueByKeys value #js["iphoneUrl" "home"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["iphoneUrl" "home"])))
                                            (= current-url "/membership")
                                              (if (not= (gobj/getValueByKeys value #js["iphoneUrl" "membership"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["iphoneUrl" "membership"])))
                                            (string/includes? current-url "/prologmateri")
                                              (if (not= (gobj/getValueByKeys value #js["iphoneUrl" "prologmateri"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["iphoneUrl" "prologmateri"])))
                                            (string/includes? current-url "/cg")
                                              (if (not= (gobj/getValueByKeys value #js["iphoneUrl" "cg"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["iphoneUrl" "cg"])))
                                            (= current-url "/download-soal")
                                              (if (not= (gobj/getValueByKeys value #js["iphoneUrl" "downloadSoal"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["iphoneUrl" "downloadSoal"])))
                                            :else (log :url "none"))
                                        )
                                      :else 
                                        (do 
                                          (cond
                                            (= (gobj/getValueByKeys value #js["desktopRole" "inactive"]) true)
                                              (classlist/add fsd "dn")
                                            (and guest? (= (gobj/getValueByKeys value #js["desktopRole" "guest"]) true))
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["desktopImg" "guest"]) "")
                                                  (classlist/add fsd "dn")
                                                  (do 
                                                    (.setAttribute img-desktop "src" (gobj/getValueByKeys value #js["desktopImg" "guest"]))
                                                    (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["mobileImg" "guest"]))))
                                                (classlist/add fsd "db"))
                                            (and regular? (= (gobj/getValueByKeys value #js["desktopRole" "member"]) true))
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["desktopImg" "member"]) "")
                                                  (classlist/add fsd "dn")
                                                  (do 
                                                    (.setAttribute img-desktop "src" (gobj/getValueByKeys value #js["desktopImg" "member"]))
                                                    (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["mobileImg" "member"]))))
                                                (classlist/add fsd "db"))
                                            (and premium? (= (gobj/getValueByKeys value #js["desktopRole" "premium"]) true))
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["desktopImg" "premium"]) "")
                                                  (classlist/add fsd "dn")
                                                  (do 
                                                    (.setAttribute img-desktop "src" (gobj/getValueByKeys value #js["desktopImg" "premium"]))
                                                    (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["mobileImg" "premium"]))))
                                                (classlist/add fsd "db"))
                                            (= (gobj/getValueByKeys value #js["desktopRole" "all"]) true)
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["desktopImg" "all"]) "")
                                                  (classlist/add fsd "dn")
                                                  (do 
                                                    (.setAttribute img-desktop "src" (gobj/getValueByKeys value #js["desktopImg" "all"]))
                                                    (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["mobileImg" "all"]))))
                                                (classlist/add fsd "db"))
                                            :else 
                                              (do 
                                                (log :role "none")
                                                (classlist/add fsd "dn")))
                                          (cond 
                                            (= current-url "/")
                                              (do
                                                (if (not= (gobj/getValueByKeys value #js["desktopUrl" "home"]) "") (.setAttribute link-desktop "href" (gobj/getValueByKeys value #js["desktopUrl" "home"])))
                                                (if (not= (gobj/getValueByKeys value #js["mobileUrl" "home"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["mobileUrl" "home"]))))
                                            (= current-url "/membership")
                                              (do 
                                                (if (not= (gobj/getValueByKeys value #js["desktopUrl" "membership"]) "") (.setAttribute link-desktop "href" (gobj/getValueByKeys value #js["desktopUrl" "membership"])))
                                                (if (not= (gobj/getValueByKeys value #js["mobileUrl" "membership"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["mobileUrl" "membership"]))))
                                            (string/includes? current-url "/prologmateri")
                                              (do 
                                                (if (not= (gobj/getValueByKeys value #js["desktopUrl" "prologmateri"]) "") (.setAttribute link-desktop "href" (gobj/getValueByKeys value #js["desktopUrl" "prologmateri"])))
                                                (if (not= (gobj/getValueByKeys value #js["mobileUrl" "prologmateri"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["mobileUrl" "prologmateri"]))))
                                            (string/includes? current-url "/cg")
                                              (do  
                                                (if (not= (gobj/getValueByKeys value #js["desktopUrl" "cg"]) "") (.setAttribute link-desktop "href" (gobj/getValueByKeys value #js["desktopUrl" "cg"])))
                                                (if (not= (gobj/getValueByKeys value #js["mobileUrl" "cg"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["mobileUrl" "cg"]))))
                                            (= current-url "/download-soal")
                                              (do 
                                                (if (not= (gobj/getValueByKeys value #js["desktopUrl" "downloadSoal"]) "") (.setAttribute link-desktop "href" (gobj/getValueByKeys value #js["desktopUrl" "downloadSoal"])))
                                                (if (not= (gobj/getValueByKeys value #js["mobileUrl" "downloadSoal"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["mobileUrl" "downloadSoal"]))))
                                            :else (log :url "none"))
                                        )
                                    ))
                                )))
                            (do
                              (log :status (str "campaign is about to be started on :" startDateText))))
                          (log :campaign-status (str "campaign status is activated")))

                      ; event listener for flash sale close button
                      (. close-elem addEventListener "click" 
                        (fn [evt]
                          (this-as this
                                  (.add (.-classList fsd) "hidden-fsd"))))
                      ))
                      ]
    
    (-> promise (p/then success-cb-fn))                  
  )
page)