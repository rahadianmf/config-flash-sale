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
            "https://raw.githubusercontent.com/rahadianmf/config-flash-sale/master/config-test.json"
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
                                              (classlist/add fsd "db")
                                            (and regular? (= (gobj/getValueByKeys value #js["mobileRole" "member"]) true))
                                              (classlist/add fsd "db")
                                            (and premium? (= (gobj/getValueByKeys value #js["mobileRole" "premium"]) true))
                                              (classlist/add fsd "db")
                                            (= (gobj/getValueByKeys value #js["mobileRole" "all"]) true)
                                              (classlist/add fsd "db")
                                            :else 
                                              (do 
                                                (log :role "none")
                                                (classlist/add fsd "dn")))
                                          (cond 
                                            (= current-url "/")
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["mobileImg" "home"]) "")
                                                  (classlist/add fsd "dn")
                                                  (do 
                                                    (.setAttribute img-desktop "src" (gobj/getValueByKeys value #js["mobileImg" "home"]))
                                                    (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["mobileImg" "home"])))) 
                                                (if (not= (gobj/getValueByKeys value #js["mobileUrl" "home"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["mobileUrl" "home"]))))
                                            (= current-url "/membership")
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["mobileImg" "membership"]) "")
                                                  (classlist/add fsd "dn")
                                                  (do 
                                                    (.setAttribute img-desktop "src" (gobj/getValueByKeys value #js["mobileImg" "membership"]))
                                                    (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["mobileImg" "membership"])))) 
                                                (if (not= (gobj/getValueByKeys value #js["mobileUrl" "membership"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["mobileUrl" "membership"]))))
                                            (string/includes? current-url "/prologmateri")
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["mobileImg" "prologmateri"]) "")
                                                  (classlist/add fsd "dn")
                                                  (do 
                                                    (.setAttribute img-desktop "src" (gobj/getValueByKeys value #js["mobileImg" "prologmateri"]))
                                                    (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["mobileImg" "prologmateri"])))) 
                                                (if (not= (gobj/getValueByKeys value #js["mobileUrl" "prologmateri"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["mobileUrl" "prologmateri"]))))
                                            (string/includes? current-url "/cg")
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["mobileImg" "cg"]) "")
                                                  (classlist/add fsd "dn")
                                                  (do 
                                                    (.setAttribute img-desktop "src" (gobj/getValueByKeys value #js["mobileImg" "cg"]))
                                                    (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["mobileImg" "cg"]))))   
                                                (if (not= (gobj/getValueByKeys value #js["mobileUrl" "cg"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["mobileUrl" "cg"]))))
                                            (= current-url "/download-soal")
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["mobileImg" "downloadSoal"]) "")
                                                  (classlist/add fsd "dn")
                                                  (do 
                                                    (.setAttribute img-desktop "src" (gobj/getValueByKeys value #js["mobileImg" "downloadSoal"]))
                                                    (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["mobileImg" "downloadSoal"])))) 
                                                (if (not= (gobj/getValueByKeys value #js["mobileUrl" "downloadSoal"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["mobileUrl" "downloadSoal"]))))
                                            :else (log :url "none"))
                                        )
                                      (userAgent.platform/isMacintosh) 
                                        (do 
                                          (cond 
                                            (= (gobj/getValueByKeys value #js["macintoshRole" "inactive"]) true)
                                              (classlist/add fsd "dn")
                                            (and guest? (= (gobj/getValueByKeys value #js["macintoshRole" "guest"]) true))
                                              (classlist/add fsd "db")
                                            (and regular? (= (gobj/getValueByKeys value #js["macintoshRole" "member"]) true))
                                              (classlist/add fsd "db")
                                            (and premium? (= (gobj/getValueByKeys value #js["macintoshRole" "premium"]) true))
                                              (classlist/add fsd "db")
                                            (= (gobj/getValueByKeys value #js["macintoshRole" "all"]) true)
                                              (classlist/add fsd "db")
                                            :else 
                                              (do 
                                                (log :role "none")
                                                (classlist/add fsd "dn")))
                                          (cond 
                                            (= current-url "/")
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["macintoshImg" "home"]) "")
                                                  (classlist/add fsd "dn")
                                                  (.setAttribute img-desktop "src" (gobj/getValueByKeys value #js["macintoshImg" "home"])))
                                                (if (not= (gobj/getValueByKeys value #js["macintoshUrl" "home"]) "") (.setAttribute link-desktop "href" (gobj/getValueByKeys value #js["macintoshUrl" "home"]))))
                                            (= current-url "/membership")
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["macintoshImg" "membership"]) "")
                                                  (classlist/add fsd "dn")
                                                  (.setAttribute img-desktop "src" (gobj/getValueByKeys value #js["macintoshImg" "membership"])))
                                                (if (not= (gobj/getValueByKeys value #js["macintoshUrl" "membership"]) "") (.setAttribute link-desktop "href" (gobj/getValueByKeys value #js["macintoshUrl" "membership"]))))
                                            (string/includes? current-url "/prologmateri")
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["macintoshImg" "prologmateri"]) "")
                                                  (classlist/add fsd "dn")
                                                  (.setAttribute img-desktop "src" (gobj/getValueByKeys value #js["desktopImg" "prologmateri"]))) 
                                                (if (not= (gobj/getValueByKeys value #js["macintoshUrl" "prologmateri"]) "") (.setAttribute link-desktop "href" (gobj/getValueByKeys value #js["macintoshUrl" "prologmateri"]))))
                                            (string/includes? current-url "/cg")
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["desktopImg" "cg"]) "")
                                                  (classlist/add fsd "dn")
                                                    (.setAttribute img-desktop "src" (gobj/getValueByKeys value #js["macintoshImg" "cg"]))) 
                                                (if (not= (gobj/getValueByKeys value #js["macintoshUrl" "cg"]) "") (.setAttribute link-desktop "href" (gobj/getValueByKeys value #js["macintoshUrl" "cg"]))))
                                            (= current-url "/download-soal")
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["macintoshImg" "downloadSoal"]) "")
                                                  (classlist/add fsd "dn")
                                                    (.setAttribute img-desktop "src" (gobj/getValueByKeys value #js["macintoshImg" "downloadSoal"]))) 
                                                (if (not= (gobj/getValueByKeys value #js["macintoshUrl" "downloadSoal"]) "") (.setAttribute link-desktop "href" (gobj/getValueByKeys value #js["macintoshUrl" "downloadSoal"]))))
                                            :else (log :url "none"))
                                        )
                                      (userAgent.platform/isIphone) 
                                        (do 
                                          (cond 
                                            (= (gobj/getValueByKeys value #js["iphoneRole" "inactive"]) true)
                                              (classlist/add fsd "dn")
                                            (and guest? (= (gobj/getValueByKeys value #js["iphoneRole" "guest"]) true))
                                              (classlist/add fsd "db")
                                            (and regular? (= (gobj/getValueByKeys value #js["iphoneRole" "member"]) true))
                                              (classlist/add fsd "db")
                                            (and premium? (= (gobj/getValueByKeys value #js["iphoneRole" "premium"]) true))
                                              (classlist/add fsd "db")
                                            (= (gobj/getValueByKeys value #js["iphoneRole" "all"]) true)
                                              (classlist/add fsd "db")
                                            :else 
                                              (do 
                                                (log :role "none")
                                                (classlist/add fsd "dn")))
                                          (cond 
                                            (= current-url "/")
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["iphoneImg" "home"]) "")
                                                  (classlist/add fsd "dn")
                                                  (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["iphoneImg" "home"]))) 
                                                (if (not= (gobj/getValueByKeys value #js["iphoneUrl" "home"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["iphoneUrl" "home"]))))
                                            (= current-url "/membership")
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["iphoneImg" "cg"]) "")
                                                  (classlist/add fsd "dn")
                                                  (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["iphoneImg" "membership"]))) 
                                                (if (not= (gobj/getValueByKeys value #js["iphoneUrl" "membership"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["iphoneUrl" "membership"]))))
                                            (string/includes? current-url "/prologmateri")
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["iphoneImg" "cg"]) "")
                                                  (classlist/add fsd "dn")
                                                  (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["iphoneImg" "prologmateri"]))) 
                                                (if (not= (gobj/getValueByKeys value #js["iphoneUrl" "prologmateri"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["iphoneUrl" "prologmateri"]))))
                                            (string/includes? current-url "/cg")
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["iphoneImg" "cg"]) "")
                                                  (classlist/add fsd "dn")
                                                  (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["iphoneImg" "cg"]))) 
                                                (if (not= (gobj/getValueByKeys value #js["iphoneUrl" "cg"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["iphoneUrl" "cg"]))))
                                            (= current-url "/download-soal")
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["iphoneImg" "cg"]) "")
                                                  (classlist/add fsd "dn")
                                                    (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["iphoneImg" "downloadSoal"]))) 
                                                (if (not= (gobj/getValueByKeys value #js["iphoneUrl" "downloadSoal"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["iphoneUrl" "downloadSoal"]))))
                                            :else (log :url "none"))
                                        )
                                      :else 
                                        (do 
                                          (cond
                                            (= (gobj/getValueByKeys value #js["desktopRole" "inactive"]) true)
                                              (classlist/add fsd "dn")
                                            (and guest? (= (gobj/getValueByKeys value #js["desktopRole" "guest"]) true))
                                              (classlist/add fsd "db")
                                            (and regular? (= (gobj/getValueByKeys value #js["desktopRole" "member"]) true))
                                              (classlist/add fsd "db")
                                            (and premium? (= (gobj/getValueByKeys value #js["desktopRole" "premium"]) true))
                                              (classlist/add fsd "db")
                                            (= (gobj/getValueByKeys value #js["desktopRole" "all"]) true)
                                              (classlist/add fsd "db")
                                            :else 
                                              (do 
                                                (log :role "none")
                                                (classlist/add fsd "dn")))
                                          (cond 
                                            (= current-url "/")
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["desktopImg" "home"]) "")
                                                  (classlist/add fsd "dn")
                                                  (do 
                                                    (.setAttribute img-desktop "src" (gobj/getValueByKeys value #js["desktopImg" "home"]))
                                                    (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["mobileImg" "home"]))))
                                                (if (not= (gobj/getValueByKeys value #js["desktopUrl" "home"]) "") (.setAttribute link-desktop "href" (gobj/getValueByKeys value #js["desktopUrl" "home"])))
                                                (if (not= (gobj/getValueByKeys value #js["mobileUrl" "home"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["mobileUrl" "home"]))))
                                            (= current-url "/membership")
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["desktopImg" "membership"]) "")
                                                  (classlist/add fsd "dn")
                                                  (do 
                                                    (.setAttribute img-desktop "src" (gobj/getValueByKeys value #js["desktopImg" "membership"]))
                                                    (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["mobileImg" "membership"]))))
                                                (if (not= (gobj/getValueByKeys value #js["desktopUrl" "membership"]) "") (.setAttribute link-desktop "href" (gobj/getValueByKeys value #js["desktopUrl" "membership"])))
                                                (if (not= (gobj/getValueByKeys value #js["mobileUrl" "membership"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["mobileUrl" "membership"]))))
                                            (string/includes? current-url "/prologmateri")
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["desktopImg" "prologmateri"]) "")
                                                  (classlist/add fsd "dn")
                                                  (do 
                                                    (.setAttribute img-desktop "src" (gobj/getValueByKeys value #js["desktopImg" "prologmateri"]))
                                                    (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["mobileImg" "prologmateri"])))) 
                                                (if (not= (gobj/getValueByKeys value #js["desktopUrl" "prologmateri"]) "") (.setAttribute link-desktop "href" (gobj/getValueByKeys value #js["desktopUrl" "prologmateri"])))
                                                (if (not= (gobj/getValueByKeys value #js["mobileUrl" "prologmateri"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["mobileUrl" "prologmateri"]))))
                                            (string/includes? current-url "/cg")
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["desktopImg" "cg"]) "")
                                                  (classlist/add fsd "dn")
                                                  (do 
                                                    (.setAttribute img-desktop "src" (gobj/getValueByKeys value #js["desktopImg" "cg"]))
                                                    (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["mobileImg" "cg"])))) 
                                                (if (not= (gobj/getValueByKeys value #js["desktopUrl" "cg"]) "") (.setAttribute link-desktop "href" (gobj/getValueByKeys value #js["desktopUrl" "cg"])))
                                                (if (not= (gobj/getValueByKeys value #js["mobileUrl" "cg"]) "") (.setAttribute link-mobile "href" (gobj/getValueByKeys value #js["mobileUrl" "cg"]))))
                                            (= current-url "/download-soal")
                                              (do 
                                                (if (= (gobj/getValueByKeys value #js["desktopImg" "downloadSoal"]) "")
                                                  (classlist/add fsd "dn")
                                                  (do 
                                                    (.setAttribute img-desktop "src" (gobj/getValueByKeys value #js["desktopImg" "downloadSoal"]))
                                                    (.setAttribute img-mobile "src" (gobj/getValueByKeys value #js["desktopImg" "downloadSoal"])))) 
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