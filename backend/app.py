from app import create_app

app = create_app()

if __name__ == "__main__":
    using_real_device = False

    if using_real_device:
        app.run(host="0.0.0.0", port=5000, debug=True)
    else:
        app.run(debug=True)
