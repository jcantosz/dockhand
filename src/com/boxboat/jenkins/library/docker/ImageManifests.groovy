package com.boxboat.jenkins.library.docker

class ImageManifests implements Serializable {
    Map<String, List<ImageManifest>> manifests = [:]

    def addManifest(def manifest){
        if( manifest && manifest.name && manifest.updatedAt && manifest.digest ){
            ImageManifest imageManifest = new ImageManifest(manifest)
            String digest = imageManifest.digest

            if (manifests.containsKey(digest)) {
                manifests.get(digest).add(imageManifest)
            } else {
                manifests.put(digest, [imageManifest])
            }
        }
    }

    List<String> getCleanableTagsList(def retentionDays = 15){
        Date now = new Date()
        long nowSec = now.getTime()

        Set keys = manifests.keySet()
        List<String> cleanableTagsList = []
        for (key in keys) {
            List<ImageManifest> imageManifests = manifests[key]
            if (imageManifests.size() == 1) {
                // No other tags attached to that digest, check the tag name and age
                def manifest = imageManifests[0]
                if (manifest.isCommitHash() && manifest.ageInDays(nowSec) > 15) {
                    cleanableTagsList.add(manifest.tag)
                }
            }
        }
        return cleanableTagsList
    }
}