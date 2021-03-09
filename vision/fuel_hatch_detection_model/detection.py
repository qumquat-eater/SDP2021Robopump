import detect_shapes as ds
import custom_model_images as cm
import cv2
import argparse


def detect_fuel_cap(image_path):
    image_np, img_coords = cm.run(image_path)
    crop_img = image_np[img_coords[0]:img_coords[2], img_coords[1]:img_coords[3]]
    cv2.imwrite("output/cropped.png", crop_img)

    ds.find_slant_angle(crop_img)
    ds.find_shape(crop_img)



ap = argparse.ArgumentParser()
ap.add_argument("-i", "--image", required=True,
    help="path to the input image")
args = vars(ap.parse_args())

detect_fuel_cap(args["image"])
